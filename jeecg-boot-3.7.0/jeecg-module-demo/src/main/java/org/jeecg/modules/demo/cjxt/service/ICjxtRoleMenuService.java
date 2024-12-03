package org.jeecg.modules.demo.cjxt.service;

import org.jeecg.modules.demo.cjxt.entity.CjxtRoleMenu;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Description: APP角色菜单配置
 * @Author: jeecg-boot
 * @Date:   2024-10-09
 * @Version: V1.0
 */
public interface ICjxtRoleMenuService extends IService<CjxtRoleMenu> {

    public void saveRolePermission(String roleId,String permissionIds,String lastPermissionIds);

}
