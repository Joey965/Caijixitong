package org.jeecg.modules.demo.cjxt.utils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.jeecg.common.util.MinioUtil;
import org.jeecg.modules.demo.cjxt.entity.CjxtXtcs;
import org.jeecg.modules.demo.cjxt.service.ICjxtXtcsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class MyStartupListener implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private ICjxtXtcsService cjxtXtcsService;
    private WatchService watchService;

    private Path pathToScan;
    private Map<WatchKey, Path> keys = new HashMap<>();

    private volatile boolean monitoringEnabled = false;

    private ExecutorService executorService;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        List<CjxtXtcs> list = cjxtXtcsService.list(new QueryWrapper<CjxtXtcs>().eq("cs_key", "fileUrl").eq("sfqy","1"));
        System.err.println(list);
        // 检查是否开启监控
        if (list.size()!=0) {
            monitoringEnabled=true;
            pathToScan = Paths.get(list.get(0).getCsVal());
            startDirectoryWatchService(pathToScan);
        } else {
            System.out.println("监控未开启");
        }
    }


    public void startMonitoring() {
        List<CjxtXtcs> list = cjxtXtcsService.list(new QueryWrapper<CjxtXtcs>().eq("cs_key", "fileUrl").eq("sfqy","1"));
        if (!monitoringEnabled) {
            monitoringEnabled = true;
            pathToScan=Paths.get(list.get(0).getCsVal());
            startDirectoryWatchService(Paths.get(list.get(0).getCsVal()));
        }
    }

    public void stopMonitoring() {
        if (monitoringEnabled) {
            monitoringEnabled = false;
            try {
                if (watchService != null) {
                    watchService.close();
                }
                if (executorService != null) {
                    executorService.shutdownNow();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("监控已停止");
        }

    }

    private void startDirectoryWatchService(Path dir) {
        try {
            watchService = FileSystems.getDefault().newWatchService();
            executorService = Executors.newSingleThreadExecutor();
            registerAllDirectories(dir, watchService);
            System.out.println("监控目录：" + dir.toString());

            executorService.submit(() -> {
                try {
                    WatchKey key;
                    while (monitoringEnabled && (key = watchService.take()) != null) {
                        Path directory = keys.get(key);
                        if (directory == null) {
                            System.err.println("无法找到对应目录");
                            continue;
                        }
                        for (WatchEvent<?> event : key.pollEvents()) {
                            WatchEvent.Kind<?> kind = event.kind();
                            @SuppressWarnings("unchecked")
                            WatchEvent<Path> ev = (WatchEvent<Path>) event;
                            Path fileName = ev.context();
                            Path filePath = directory.resolve(fileName);
                            if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                                if (Files.isDirectory(filePath)) {
                                    System.out.println(filePath + "=========");
                                    registerAllDirectories(filePath, watchService);
                                    processNewDirectory(filePath); // 处理新目录中的所有文件
                                } else {
                                    System.out.println("发现新文件: " + filePath);
                                    processFile(filePath);
                                }
                            }
                        }
                        boolean valid = key.reset();
                        if (!valid) {
                            keys.remove(key);
                            if (keys.isEmpty()) {
                                break;
                            }
                        }
                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processNewDirectory(Path directory) throws IOException {
        Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                processFile(file);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private void registerAllDirectories(Path start, WatchService watchService) throws IOException {
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                WatchKey key = dir.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
                keys.put(key, dir);
                System.out.println("已注册目录: " + dir + "，事件: " + StandardWatchEventKinds.ENTRY_CREATE);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                System.out.println("已处理目录: " + dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private void processFile(Path file) throws IOException {
        BasicFileAttributes attrs1 = Files.readAttributes(file, BasicFileAttributes.class);
        System.out.println("Readable: " + Files.isReadable(file));
        System.out.println("Writable: " + Files.isWritable(file));
        System.out.println("Executable: " + Files.isExecutable(file));
        try {
            Thread.sleep(500); // 500毫秒
            try (InputStream fis = Files.newInputStream(file)) {
                // 将相对路径转换为 MinIO 中的路径
                Path relativePath = pathToScan.relativize(file);
                String minioPath = relativePath.toString().replace("\\", "/");
                // 上传文件到 MinIO
                MinioUtil.upload(fis, minioPath);

                // 确保文件被成功上传后再删除
                if (Files.exists(file)) {
                    Files.delete(file);
                    System.out.println("文件上传成功并已删除: " + file);
                    deleteEmptyDirectories(file.getParent(), pathToScan);
                } else {
                    System.out.println("文件已不存在，跳过删除: " + file);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void deleteEmptyDirectories(Path dir, Path rootDir) throws IOException {
        // 仅从监听目录开始向上遍历
        for (Path currentDir = dir; currentDir != null && !currentDir.equals(rootDir); currentDir = currentDir.getParent()) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(currentDir)) {
                // 如果目录为空，则删除
                if (!stream.iterator().hasNext()) {
                    Files.delete(currentDir);
                    System.out.println("已删除空目录: " + currentDir);
                } else {
                    // 如果目录不为空，则退出
                    break;
                }
            }
        }
    }
}
