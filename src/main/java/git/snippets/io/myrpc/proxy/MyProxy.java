package git.snippets.io.myrpc.proxy;

import git.snippets.io.myrpc.rpc.Dispacher;
import git.snippets.io.myrpc.rpc.protocol.MyContent;
import git.snippets.io.myrpc.rpc.transport.ClientFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.CompletableFuture;

/**
 * @author <a href="mailto:410486047@qq.com">Grey</a>
 * @date 2022/1/1
 * @since
 */
public class MyProxy {
    public static <T> T proxyGet(Class<T> clazz) {
        ClassLoader loader = clazz.getClassLoader();
        Class<?>[] methodInfo = {clazz};
        Dispacher dis = Dispacher.getDis();
        return (T) Proxy.newProxyInstance(loader, methodInfo, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                Object result = null;
                // 0. 确定方法是远程还是RPC调用
                Object o = dis.get(clazz.getName());
                Object res = null;
                if (o == null) {
                    System.out.println("rpc call...");
                    // 1. 调用服务，方法，参数--》封装成message
                    String name = clazz.getName();
                    String methodName = method.getName();
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    MyContent context = new MyContent();
                    context.setMethodName(methodName);
                    context.setName(name);
                    context.setParameterTypes(parameterTypes);
                    context.setArgs(args);

                    /**
                     * 1. 缺失了注册发现
                     * 2. 第一层负载：面向的provider
                     * 3. consumer 线程池 面向service；并发就有木桶效应，倾斜
                     * serviceA
                     *      ipA:port
                     *         socket1
                     *         socket2
                     *      ipB:port
                     *
                     */
                    CompletableFuture resF = ClientFactory.transport(context);
                    res = resF.get();//阻塞的

                } else {
                    System.out.println("local fc...");
                    // 就是local
                    Class<?> clazz = o.getClass();
                    Method m = null;

                    try {
                        m = clazz.getMethod(method.getName(), method.getParameterTypes());
                        res = m.invoke(o, args);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                }
                return res;
            }
        });
    }
}
