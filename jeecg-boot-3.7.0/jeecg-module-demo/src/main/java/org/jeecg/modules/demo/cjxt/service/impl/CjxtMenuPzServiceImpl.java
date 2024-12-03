package org.jeecg.modules.demo.cjxt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.common.system.vo.SelectTreeModel;
import org.jeecg.modules.demo.cjxt.entity.CjxtMenuPz;
import org.jeecg.modules.demo.cjxt.mapper.CjxtMenuPzMapper;
import org.jeecg.modules.demo.cjxt.service.ICjxtMenuPzService;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * @Description: APP功能菜单
 * @Author: jeecg-boot
 * @Date:   2024-10-09
 * @Version: V1.0
 */
@Service
public class CjxtMenuPzServiceImpl extends ServiceImpl<CjxtMenuPzMapper, CjxtMenuPz> implements ICjxtMenuPzService {

	@Override
	public void addCjxtMenuPz(CjxtMenuPz cjxtMenuPz) {
	   //自定义子级父名称
        String parentName="";
        //新增时设置hasChild为0
	    cjxtMenuPz.setHasChild(ICjxtMenuPzService.NOCHILD);
		if(oConvertUtils.isEmpty(cjxtMenuPz.getPid())){
			cjxtMenuPz.setPid(ICjxtMenuPzService.ROOT_PID_VALUE);
		}else{
			//如果当前节点父ID不为空 则设置父节点的hasChildren 为1
			CjxtMenuPz parent = baseMapper.selectById(cjxtMenuPz.getPid());
			if(parent!=null && !"1".equals(parent.getHasChild())){
				parent.setHasChild("1");
				baseMapper.updateById(parent);
			}
            //子级父名称
            if(parent!=null && !"".equals(parent.getAppName()) && parent.getAppName()!=null){
                parentName = parent.getAppName();
            }
		}
        if("0".equals(cjxtMenuPz.getPid())){
            cjxtMenuPz.setParentName(cjxtMenuPz.getAppName());
        }
        if(!"0".equals(cjxtMenuPz.getPid())){
            cjxtMenuPz.setParentName(parentName);
        }
		baseMapper.insert(cjxtMenuPz);
	}
	
	@Override
	public void updateCjxtMenuPz(CjxtMenuPz cjxtMenuPz) {
		CjxtMenuPz entity = this.getById(cjxtMenuPz.getId());
		if(entity==null) {
			throw new JeecgBootException("未找到对应实体");
		}
		String old_pid = entity.getPid();
		String new_pid = cjxtMenuPz.getPid();
		if(!old_pid.equals(new_pid)) {
			updateOldParentNode(old_pid);
			if(oConvertUtils.isEmpty(new_pid)){
				cjxtMenuPz.setPid(ICjxtMenuPzService.ROOT_PID_VALUE);
			}
			if(!ICjxtMenuPzService.ROOT_PID_VALUE.equals(cjxtMenuPz.getPid())) {
				baseMapper.updateTreeNodeStatus(cjxtMenuPz.getPid(), ICjxtMenuPzService.HASCHILD);
			}
		}
        if("1".equals(entity.getHasChild())){
            //父级parentName
            cjxtMenuPz.setParentName(entity.getAppName());
            //父级下所有子级修改
            List<CjxtMenuPz> menuPzList = baseMapper.selectList(new LambdaQueryWrapper<CjxtMenuPz>().eq(CjxtMenuPz::getPid,entity.getId()));
            for(CjxtMenuPz menuPz: menuPzList){
                menuPz.setParentName(entity.getAppName());
                baseMapper.updateById(menuPz);
            }
        }
		baseMapper.updateById(cjxtMenuPz);
	}
	
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteCjxtMenuPz(String id) throws JeecgBootException {
		//查询选中节点下所有子节点一并删除
        id = this.queryTreeChildIds(id);
        if(id.indexOf(",")>0) {
            StringBuffer sb = new StringBuffer();
            String[] idArr = id.split(",");
            for (String idVal : idArr) {
                if(idVal != null){
                    CjxtMenuPz cjxtMenuPz = this.getById(idVal);
                    String pidVal = cjxtMenuPz.getPid();
                    //查询此节点上一级是否还有其他子节点
                    List<CjxtMenuPz> dataList = baseMapper.selectList(new QueryWrapper<CjxtMenuPz>().eq("pid", pidVal).notIn("id",Arrays.asList(idArr)));
                    boolean flag = (dataList == null || dataList.size() == 0) && !Arrays.asList(idArr).contains(pidVal) && !sb.toString().contains(pidVal);
                    if(flag){
                        //如果当前节点原本有子节点 现在木有了，更新状态
                        sb.append(pidVal).append(",");
                    }
                }
            }
            //批量删除节点
            baseMapper.deleteBatchIds(Arrays.asList(idArr));
            //修改已无子节点的标识
            String[] pidArr = sb.toString().split(",");
            for(String pid : pidArr){
                this.updateOldParentNode(pid);
            }
        }else{
            CjxtMenuPz cjxtMenuPz = this.getById(id);
            if(cjxtMenuPz==null) {
                throw new JeecgBootException("未找到对应实体");
            }
            updateOldParentNode(cjxtMenuPz.getPid());
            baseMapper.deleteById(id);
        }
	}
	
	@Override
    public List<CjxtMenuPz> queryTreeListNoPage(QueryWrapper<CjxtMenuPz> queryWrapper) {
        List<CjxtMenuPz> dataList = baseMapper.selectList(queryWrapper);
        List<CjxtMenuPz> mapList = new ArrayList<>();
        for(CjxtMenuPz data : dataList){
            String pidVal = data.getPid();
            //递归查询子节点的根节点
            if(pidVal != null && !ICjxtMenuPzService.NOCHILD.equals(pidVal)){
                CjxtMenuPz rootVal = this.getTreeRoot(pidVal);
                if(rootVal != null && !mapList.contains(rootVal)){
                    mapList.add(rootVal);
                }
            }else{
                if(!mapList.contains(data)){
                    mapList.add(data);
                }
            }
        }
        return mapList;
    }

    @Override
    public List<SelectTreeModel> queryListByCode(String parentCode) {
        String pid = ROOT_PID_VALUE;
        if (oConvertUtils.isNotEmpty(parentCode)) {
            LambdaQueryWrapper<CjxtMenuPz> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(CjxtMenuPz::getPid, parentCode);
            List<CjxtMenuPz> list = baseMapper.selectList(queryWrapper);
            if (list == null || list.size() == 0) {
                throw new JeecgBootException("该编码【" + parentCode + "】不存在，请核实!");
            }
            if (list.size() > 1) {
                throw new JeecgBootException("该编码【" + parentCode + "】存在多个，请核实!");
            }
            pid = list.get(0).getId();
        }
        return baseMapper.queryListByPid(pid, null);
    }

    @Override
    public List<SelectTreeModel> queryListByPid(String pid) {
        if (oConvertUtils.isEmpty(pid)) {
            pid = ROOT_PID_VALUE;
        }
        return baseMapper.queryListByPid(pid, null);
    }

	/**
	 * 根据所传pid查询旧的父级节点的子节点并修改相应状态值
	 * @param pid
	 */
	private void updateOldParentNode(String pid) {
		if(!ICjxtMenuPzService.ROOT_PID_VALUE.equals(pid)) {
			Long count = baseMapper.selectCount(new QueryWrapper<CjxtMenuPz>().eq("pid", pid));
			if(count==null || count<=1) {
				baseMapper.updateTreeNodeStatus(pid, ICjxtMenuPzService.NOCHILD);
			}
		}
	}

	/**
     * 递归查询节点的根节点
     * @param pidVal
     * @return
     */
    private CjxtMenuPz getTreeRoot(String pidVal){
        CjxtMenuPz data =  baseMapper.selectById(pidVal);
        if(data != null && !ICjxtMenuPzService.ROOT_PID_VALUE.equals(data.getPid())){
            return this.getTreeRoot(data.getPid());
        }else{
            return data;
        }
    }

    /**
     * 根据id查询所有子节点id
     * @param ids
     * @return
     */
    private String queryTreeChildIds(String ids) {
        //获取id数组
        String[] idArr = ids.split(",");
        StringBuffer sb = new StringBuffer();
        for (String pidVal : idArr) {
            if(pidVal != null){
                if(!sb.toString().contains(pidVal)){
                    if(sb.toString().length() > 0){
                        sb.append(",");
                    }
                    sb.append(pidVal);
                    this.getTreeChildIds(pidVal,sb);
                }
            }
        }
        return sb.toString();
    }

    /**
     * 递归查询所有子节点
     * @param pidVal
     * @param sb
     * @return
     */
    private StringBuffer getTreeChildIds(String pidVal,StringBuffer sb){
        List<CjxtMenuPz> dataList = baseMapper.selectList(new QueryWrapper<CjxtMenuPz>().eq("pid", pidVal));
        if(dataList != null && dataList.size()>0){
            for(CjxtMenuPz tree : dataList) {
                if(!sb.toString().contains(tree.getId())){
                    sb.append(",").append(tree.getId());
                }
                this.getTreeChildIds(tree.getId(),sb);
            }
        }
        return sb;
    }

}
