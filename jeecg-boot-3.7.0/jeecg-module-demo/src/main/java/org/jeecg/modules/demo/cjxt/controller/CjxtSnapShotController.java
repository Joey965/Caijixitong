package org.jeecg.modules.demo.cjxt.controller;

import java.util.Arrays;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.demo.cjxt.entity.CjxtSnapShot;
import org.jeecg.modules.demo.cjxt.service.ICjxtSnapShotService;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;

import org.jeecg.common.system.base.controller.JeecgController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.jeecg.common.aspect.annotation.AutoLog;

 /**
 * @Description: 随手拍
 * @Author: jeecg-boot
 * @Date:   2024-08-19
 * @Version: V1.0
 */
@Api(tags="随手拍")
@RestController
@RequestMapping("/cjxt/cjxtSnapShot")
@Slf4j
public class CjxtSnapShotController extends JeecgController<CjxtSnapShot, ICjxtSnapShotService> {
	@Autowired
	private ICjxtSnapShotService cjxtSnapShotService;


	 //minio图片服务器
	 @Value(value="${jeecg.minio.minio_url}")
	 private String minioUrl;
	 @Value(value="${jeecg.minio.bucketName}")
	 private String bucketName;

	/**
	 * 分页列表查询
	 *
	 * @param cjxtSnapShot
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "随手拍-分页列表查询")
	@ApiOperation(value="随手拍-分页列表查询", notes="随手拍-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<CjxtSnapShot>> queryPageList(CjxtSnapShot cjxtSnapShot,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<CjxtSnapShot> queryWrapper = QueryGenerator.initQueryWrapper(cjxtSnapShot, req.getParameterMap());
		Page<CjxtSnapShot> page = new Page<CjxtSnapShot>(pageNo, pageSize);
		IPage<CjxtSnapShot> pageList = cjxtSnapShotService.page(page, queryWrapper);
		pageList.getRecords().forEach(item ->{
			item.setSbPic(minioUrl+"/"+bucketName+"/"+item.getSbPic());
		});
		return Result.OK(pageList);
	}

	 @ApiOperation(value="随手拍-分页列表查询", notes="随手拍-分页列表查询")
	 @GetMapping(value = "/appList")
	 public Result<IPage<CjxtSnapShot>> appList(CjxtSnapShot cjxtSnapShot,
													  @RequestParam(name="sblx", required = false) String sblx,
													  @RequestParam(name="search", required = false) String search,
													  @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
													  @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
													  HttpServletRequest req) {
		 QueryWrapper<CjxtSnapShot> queryWrapper = QueryGenerator.initQueryWrapper(cjxtSnapShot, req.getParameterMap());
		 if(!"".equals(sblx) && sblx!=null){
			 queryWrapper.eq("sb_type",sblx);
		 }
		 if(!"".equals(search) && search!=null){
			 queryWrapper.like("sb_title",search);
		 }
		 queryWrapper.orderByDesc("create_time");
		 Page<CjxtSnapShot> page = new Page<CjxtSnapShot>(pageNo, pageSize);
		 IPage<CjxtSnapShot> pageList = cjxtSnapShotService.page(page, queryWrapper);
		 pageList.getRecords().forEach(item ->{
			 item.setSbPic(minioUrl+"/"+bucketName+"/"+item.getSbPic());
		 });
		 return Result.OK(pageList);
	 }

	/**
	 *   添加
	 *
	 * @param cjxtSnapShot
	 * @return
	 */
	@AutoLog(value = "随手拍-添加")
	@ApiOperation(value="随手拍-添加", notes="随手拍-添加")
	//@RequiresPermissions("cjxt:cjxt_snap_shot:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody CjxtSnapShot cjxtSnapShot) {
		String heardUrl = minioUrl+"/"+bucketName+"/";
		if(cjxtSnapShot.getSbPic()!=null && !"".equals(cjxtSnapShot.getSbPic())){
			if(cjxtSnapShot.getSbPic().contains(heardUrl)){
				cjxtSnapShot.setSbPic(cjxtSnapShot.getSbPic().replace(heardUrl,""));
			}
		}
		cjxtSnapShot.setSbTime(new Date());
		cjxtSnapShotService.save(cjxtSnapShot);
		return Result.OK("添加成功！");
	}

	/**
	 *  编辑
	 *
	 * @param cjxtSnapShot
	 * @return
	 */
	@AutoLog(value = "随手拍-编辑")
	@ApiOperation(value="随手拍-编辑", notes="随手拍-编辑")
	//@RequiresPermissions("cjxt:cjxt_snap_shot:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody CjxtSnapShot cjxtSnapShot) {
		String heardUrl = minioUrl+"/"+bucketName+"/";
		if(cjxtSnapShot.getSbPic()!=null && !"".equals(cjxtSnapShot.getSbPic())){
			if(cjxtSnapShot.getSbPic().contains(heardUrl)){
				cjxtSnapShot.setSbPic(cjxtSnapShot.getSbPic().replace(heardUrl,""));
			}
		}
		cjxtSnapShotService.updateById(cjxtSnapShot);
		return Result.OK("编辑成功!");
	}

	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "随手拍-通过id删除")
	@ApiOperation(value="随手拍-通过id删除", notes="随手拍-通过id删除")
	//@RequiresPermissions("cjxt:cjxt_snap_shot:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		cjxtSnapShotService.removeById(id);
		return Result.OK("删除成功!");
	}

	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "随手拍-批量删除")
	@ApiOperation(value="随手拍-批量删除", notes="随手拍-批量删除")
	//@RequiresPermissions("cjxt:cjxt_snap_shot:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.cjxtSnapShotService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "随手拍-通过id查询")
	@ApiOperation(value="随手拍-通过id查询", notes="随手拍-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<CjxtSnapShot> queryById(@RequestParam(name="id",required=true) String id) {
		CjxtSnapShot cjxtSnapShot = cjxtSnapShotService.getById(id);
		if(cjxtSnapShot==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(cjxtSnapShot);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param cjxtSnapShot
    */
    //@RequiresPermissions("cjxt:cjxt_snap_shot:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, CjxtSnapShot cjxtSnapShot) {
        return super.exportXls(request, cjxtSnapShot, CjxtSnapShot.class, "随手拍");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    //@RequiresPermissions("cjxt:cjxt_snap_shot:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, CjxtSnapShot.class);
    }

}
