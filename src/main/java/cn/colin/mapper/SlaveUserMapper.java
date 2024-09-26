package cn.colin.mapper;

import cn.colin.common.entity.User;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Administrator
 */
@Mapper
@DS("slave")
public interface SlaveUserMapper extends BaseMapper<User> {

}




