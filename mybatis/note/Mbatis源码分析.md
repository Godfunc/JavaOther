### SqlSession 的创建

1.  因为`SqlSession` 的通过调用了 `sqlSessionFactory.open()` 获得的，所以我们先看如何获取 `SqlSessionFactory`  实例。

   ```java
   SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(resourceAsStream);
   ```

   根据上面的代码可知，`SqlSessionFactory` 的通过 `SqlSessionFactoryBuilder.build(arg)` 构建的实例，我们先分析 `build(arg)` 做了哪些事。来到 `SqlSessionFactoryBuild` 的源码中。

   ```java
   public class SqlSessionFactoryBuilder {
   
     public SqlSessionFactory build(Reader reader) {
       return build(reader, null, null);
     }
   
     public SqlSessionFactory build(Reader reader, String environment) {
       return build(reader, environment, null);
     }
   
     public SqlSessionFactory build(Reader reader, Properties properties) {
       return build(reader, null, properties);
     }
   
     public SqlSessionFactory build(Reader reader, String environment, Properties properties) {
       try {
         XMLConfigBuilder parser = new XMLConfigBuilder(reader, environment, properties);
         return build(parser.parse());
       } catch (Exception e) {
         throw ExceptionFactory.wrapException("Error building SqlSession.", e);
       } finally {
         ErrorContext.instance().reset();
         try {
           reader.close();
         } catch (IOException e) {
           // Intentionally ignore. Prefer previous error.
         }
       }
     }
   
     public SqlSessionFactory build(InputStream inputStream) {
       return build(inputStream, null, null);
     }
   
     public SqlSessionFactory build(InputStream inputStream, String environment) {
       return build(inputStream, environment, null);
     }
   
     public SqlSessionFactory build(InputStream inputStream, Properties properties) {
       return build(inputStream, null, properties);
     }
   
     public SqlSessionFactory build(InputStream inputStream, String environment, Properties properties) {
       try {
         XMLConfigBuilder parser = new XMLConfigBuilder(inputStream, environment, properties);
         return build(parser.parse());
       } catch (Exception e) {
         throw ExceptionFactory.wrapException("Error building SqlSession.", e);
       } finally {
         ErrorContext.instance().reset();
         try {
           inputStream.close();
         } catch (IOException e) {
           // Intentionally ignore. Prefer previous error.
         }
       }
     }
       
     public SqlSessionFactory build(Configuration config) {
       return new DefaultSqlSessionFactory(config);
     }
   
   }
   ```

   我们可以看到 `SqlSessionFactoryBuild` 可以通过 `Reader` `InputStream` `Configuration` 三种方式获取到 `mybatis` 的 xml 配置文件，进行 `SqlSessionFactory` 对象的构建 ，不过读取配置文件之后，最终都是将其转换成 `Configuration` 调用 `DefaultSqlSessionFactory` 获取 `SqlSessionFactory` 对象。

   其中将 配置文件 转成 `Configuration` 都是通过 `XMLConfigBuilder` 和 `XPathParser` 