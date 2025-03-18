package cn.ace.xiaohashu.auth.domain.mapper;

import cn.ace.xiaohashu.auth.domain.dataobject.RolePermissionRelDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RolePermissionRelDOMapper {
    int deleteByPrimaryKey(Long id);

    int insert(RolePermissionRelDO record);

    int insertSelective(RolePermissionRelDO record);

    RolePermissionRelDO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(RolePermissionRelDO record);

    int updateByPrimaryKey(RolePermissionRelDO record);

    List<RolePermissionRelDO> selectByRoleIds(@Param("roleIds") List<Long> roleIds);
}