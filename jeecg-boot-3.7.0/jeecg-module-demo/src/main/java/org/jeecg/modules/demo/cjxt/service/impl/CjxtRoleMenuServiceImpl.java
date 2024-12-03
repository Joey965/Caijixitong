package org.jeecg.modules.demo.cjxt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.jeecg.common.util.IpUtils;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.demo.cjxt.entity.CjxtMenuPz;
import org.jeecg.modules.demo.cjxt.entity.CjxtRoleMenu;
import org.jeecg.modules.demo.cjxt.mapper.CjxtRoleMenuMapper;
import org.jeecg.modules.demo.cjxt.service.ICjxtMenuPzService;
import org.jeecg.modules.demo.cjxt.service.ICjxtRoleMenuService;
import org.jeecg.modules.system.entity.SysRole;
import org.jeecg.modules.system.entity.SysRolePermission;
import org.jeecg.modules.system.service.ISysRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * @Description: APP角色菜单配置
 * @Author: jeecg-boot
 * @Date:   2024-10-09
 * @Version: V1.0
 */
@Service
public class CjxtRoleMenuServiceImpl extends ServiceImpl<CjxtRoleMenuMapper, CjxtRoleMenu> implements ICjxtRoleMenuService {

    @Autowired
    private ISysRoleService sysRoleService;
    @Autowired
    private ICjxtMenuPzService cjxtMenuPzService;

    @Override
    public void saveRolePermission(String roleId, String permissionIds, String lastPermissionIds) {
        List<String> add = getDiff(lastPermissionIds,permissionIds);
        if(add!=null && add.size()>0) {
            List<CjxtRoleMenu> list = new ArrayList<CjxtRoleMenu>();
            for (String p : add) {
                if(oConvertUtils.isNotEmpty(p)) {
                    CjxtRoleMenu rolepms = new CjxtRoleMenu(roleId, p);
                    SysRole sysRole = sysRoleService.getById(roleId);
                    if(sysRole!=null){
                        rolepms.setRoleCode(sysRole.getRoleCode());
                    }
                    list.add(rolepms);
                }
            }
            this.saveBatch(list);
        }
        List<String> delete = getDiff(permissionIds,lastPermissionIds);
        if(delete!=null && delete.size()>0) {
            for (String permissionId : delete) {
                this.remove(new QueryWrapper<CjxtRoleMenu>().lambda().eq(CjxtRoleMenu::getRoleId, roleId).eq(CjxtRoleMenu::getMenuId, permissionId));
            }
        }
    }

    /**
     * 从diff中找出main中没有的元素
     * @param main
     * @param diff
     * @return
     */
    private List<String> getDiff(String main,String diff){
        if(oConvertUtils.isEmpty(diff)) {
            return null;
        }
        if(oConvertUtils.isEmpty(main)) {
            return Arrays.asList(diff.split(","));
        }

        String[] mainArr = main.split(",");
        String[] diffArr = diff.split(",");
        Map<String, Integer> map = new HashMap(5);
        for (String string : mainArr) {
            map.put(string, 1);
        }
        List<String> res = new ArrayList<String>();
        for (String key : diffArr) {
            if(oConvertUtils.isNotEmpty(key) && !map.containsKey(key)) {
                res.add(key);
            }
        }
        return res;
    }

}
