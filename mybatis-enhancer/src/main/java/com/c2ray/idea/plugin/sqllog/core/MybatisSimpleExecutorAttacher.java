package com.c2ray.idea.plugin.sqllog.core;

import javassist.*;

import java.security.ProtectionDomain;

/**
 * @author c2ray
 */
public class MybatisSimpleExecutorAttacher extends Attacher {

    @Override
    public String getTargetClassName() {
        return "org.apache.ibatis.executor.SimpleExecutor";
    }

    @Override
    protected byte[] doTransform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                              ProtectionDomain protectionDomain, byte[] classfileBuffer) throws Exception {
        ClassPool classPool = ClassPool.getDefault();
        CtClass targetClass = classPool.get(getTargetClassName());
        CtMethod targetMethod = targetClass.getDeclaredMethod("prepareStatement");
        targetMethod.insertAfter(
                "String methodName = com.c2ray.idea.plugin.sqllog.utils.ThreadLocalUtils.getMybatisSqlId();" +
                        "String sql = $_.unwrap(java.sql.PreparedStatement.class).toString().split(\"[:|-]\", 2)[1];\n" +
                        "sql = com.c2ray.idea.plugin.sqllog.utils.StringUtils.removeExtraWhitespaces(sql);\n" +
                        "com.c2ray.idea.plugin.sqllog.utils.MessageUtils.sendMybatisProtocol(methodName, sql);" +
                        "return $_;");
        return targetClass.toBytecode();
    }

}
