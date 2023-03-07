package cn.cnaworld.framework.infrastructure.component.mybatisplus.injector;

import cn.cnaworld.framework.infrastructure.component.mybatisplus.injector.delete.DirectDelete;
import cn.cnaworld.framework.infrastructure.component.mybatisplus.injector.delete.DirectDeleteBatchByIds;
import cn.cnaworld.framework.infrastructure.component.mybatisplus.injector.delete.DirectDeleteById;
import cn.cnaworld.framework.infrastructure.component.mybatisplus.injector.delete.DirectDeleteByMap;
import cn.cnaworld.framework.infrastructure.component.mybatisplus.injector.recover.Recover;
import cn.cnaworld.framework.infrastructure.component.mybatisplus.injector.recover.RecoverBatchByIds;
import cn.cnaworld.framework.infrastructure.component.mybatisplus.injector.recover.RecoverById;
import cn.cnaworld.framework.infrastructure.component.mybatisplus.injector.recover.RecoverByMap;
import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.injector.DefaultSqlInjector;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 将自定义方法进行注册
 * @author Lucifer
 * @date 2023/3/5
 * @since 1.0.0
 */
@Slf4j
public class CustomizedSqlInjector extends DefaultSqlInjector {

    @Override
    public List<AbstractMethod> getMethodList(Class<?> mapperClass, TableInfo tableInfo) {
        //如果只需增加方法，保留mybatis plus自带方法 ， 可以先获取super.getMethodList()，再添加add
        List<AbstractMethod> methodList = super.getMethodList(mapperClass, tableInfo);
        methodList.add(new Recover("recover"));
        methodList.add(new RecoverById("recoverById"));
        methodList.add(new RecoverBatchByIds("recoverBatchIds"));
        methodList.add(new RecoverByMap("recoverByMap"));
        methodList.add(new DirectDelete("directDelete"));
        methodList.add(new DirectDeleteById("directDeleteById"));
        methodList.add(new DirectDeleteBatchByIds("directDeleteBatchIds"));
        methodList.add(new DirectDeleteByMap("directDeleteByMap"));
        return methodList;
    }

}
