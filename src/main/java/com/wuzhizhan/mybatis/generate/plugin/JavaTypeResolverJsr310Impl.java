package com.wuzhizhan.mybatis.generate.plugin;

import java.sql.JDBCType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.internal.types.JavaTypeResolverDefaultImpl;

public class JavaTypeResolverJsr310Impl extends JavaTypeResolverDefaultImpl {
    protected FullyQualifiedJavaType overrideDefaultType(IntrospectedColumn column, FullyQualifiedJavaType defaultType) {
        FullyQualifiedJavaType answer = defaultType;
        switch (column.getJdbcType()) {
            case -7:
                answer = calculateBitReplacement(column, defaultType);
                break;
            case 2:
            case 3:
                answer = calculateBigDecimalReplacement(column, defaultType);
                break;
            case 91:
                answer = new FullyQualifiedJavaType(LocalDate.class.getName());
                break;
            case 92:
                answer = new FullyQualifiedJavaType(LocalTime.class.getName());
                break;
            case 93:
                answer = new FullyQualifiedJavaType(LocalDateTime.class.getName());
                break;
        }
        return answer;
    }
}
