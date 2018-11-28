### mybatis 项目初始化

1. maven依赖

   ```xml
   <dependency>
       <groupId>org.mybatis</groupId>
       <artifactId>mybatis</artifactId>
       <version>3.4.6</version>
   </dependency>
   <dependency>
       <groupId>junit</groupId>
       <artifactId>junit</artifactId>
       <version>4.12</version>
   </dependency>
   <dependency>
       <groupId>mysql</groupId>
       <artifactId>mysql-connector-java</artifactId>
       <version>8.0.13</version>
   </dependency>
   <dependency>
       <groupId>ch.qos.logback</groupId>
       <artifactId>logback-classic</artifactId>
       <version>1.2.3</version>
   </dependency>
   ```



2. Mybatis 全局配置文件

   ```xml
   <?xml version="1.0" encoding="UTF-8" ?>
   <!DOCTYPE configuration
           PUBLIC "-//mybatis.org//DTD Config 3.0//EN"
           "http://mybatis.org/dtd/mybatis-3-config.dtd">
   <configuration>
   
       <environments default="development">
           <environment id="development">
               <transactionManager type="JDBC"/>
               <dataSource type="POOLED">
                   <property name="driver" value="com.mysql.cj.jdbc.Driver"/>
                   <property name="url" value="jdbc:mysql://192.168.31.136:3306/mybatis"/>
                   <property name="username" value="root"/>
                   <property name="password" value="123456"/>
               </dataSource>
           </environment>
       </environments>
       <mappers>
           <mapper resource="UserMapper.xml"/>
       </mappers>
   </configuration>
   ```



3. 创建实体类和Mapper配置文件

   ```java
   public class User {
       private Long id;
       private String name;
       private Integer age;
   }
   ```

   ```xml
   <?xml version="1.0" encoding="UTF-8" ?>
   <!DOCTYPE mapper
           PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
           "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
   <mapper namespace="com.godfunc.UserMapper">
       <select id="selectUser" resultType="com.godfunc.User">
       select * from user where id = #{id}
     </select>
   </mapper>
   ```


4. 测试

   ```java
   @Test
       public void func1() throws IOException {
           String resource = "mybatis-config.xml";
           InputStream resourceAsStream = Resources.getResourceAsStream(resource);
           SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(resourceAsStream);
           SqlSession sqlSession = sqlSessionFactory.openSession();
           User user = sqlSession.selectOne("com.godfunc.UserMapper.selectUser", 1L);
           log.error("result: {}", user);
       }
   sqlSession.close();
   ```



### 接口式编程

1. 创建 `UserMapper.java` 

   ```java
   public interface UserMapper {
       User selectUser(Long id);
   }
   ```

2. 将 `UserMapper.xml` 的 `namespace` 改为 `UserMapper.java` 的全类名。

3. 测试

   ```java
   @Test
       public void func2() throws IOException {
           String resource = "mybatis-config.xml";
           InputStream resourceAsStream = Resources.getResourceAsStream(resource);
           SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(resourceAsStream);
           SqlSession sqlSession = sqlSessionFactory.openSession();
           UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
           User user = userMapper.selectUser(1L);
           log.error("type {}", userMapper.getClass());
           log.error("result: {}", user);
           sqlSession.close();
       }
   ```

   此时我们看到控制台将 `userMapper` 的 type 打印出来了：

   ```console
   [main] ERROR com.godfunc.Demo1 - type class com.sun.proxy.$Proxy5
   ```

   在这里我们可以看到，`myBatis`  为 `userMapper` 创建了一个代理对象（也就是 `UserMapper` 的实现类 ），此时我们就知道了，`Mybatis` 的接口式编程，其实最终还是为接口创建了实现类，然后调用实现方法，对数据库进行操作。



4. `INSERT`  返回主键

   * 在 `UserMapper.java` 中加上 `insert`

     ```java
     void insert(User user);
     ```

   * 在 `UserMapper.xml` 中配置 `sql`

     ```xml
     <insert id="insert" parameterType="com.godfunc.User" useGeneratedKeys="true" keyProperty="id">
             INSERT INTO user(name, age) values(#{name}, #{age})
     </insert>
     ```

     *note: 如果参数是一个对象，我们是可以直接通过属性名就行取值的。*



### Mybatis 多参数

多个参数，`Mybatis` 会做特殊处理，会将多个参数封装到一个 `map` 中。`map` 的 key 是从 `param1...paramn`

1. 接口方法和 sql 配置

   ```xml
   <update id="update">
           update user set name = #{param1}, age = #{param2} where id = #{param3}
   </update>
   ```

   ```java
   void update(String name, Integer age, Long id);
   ```

   这样，我们可以通过取出 `map` 中的多个参数，进行数据的更新。

   *note: 如果参数是 `List` 获取 `Array` , 装到 `map` 中 key 的名字是 `list` 和`array`*





### Mybatis 参数封装

1. 以 `update` 为例（这里用的是@Param注解）

   ```java
   void update(@Param("name") String name, @Param("age") Integer age, @Param("id") Long id);
   ```

2. 在执行`update` 方法时，会进入 `org.apache.ibatis.binding.MapperProxy`类中，执行 `invoke` 方法，从类的命名上我们可以清楚的知道， `MapperProxy` 是Mapper的动态代理类





### 查询返回 `Map`

1. 接口和配置文件如下

   ```java
   @MapKey("id")
   Map<Long, User> selectResultMap();
   ```

   ```xml
   <select id="selectResultMap" resultType="com.godfunc.User">
       select * from user
   </select>
   ```

   > 11:23:24.543 [main] ERROR com.godfunc.Demo1 - result {1=User{id=1, name='里斯', age=2}, 3=User{id=3, name='天全', age=3}, 8=User{id=8, name='王大山', age=34}, 9=User{id=9, name='天全11', age=3}}

   Mapper 配置文件中返回值类型依旧是 `User`，在接口方法声明上使用 `@MapKey("id")` 将 返回结果中的 `id` 作为 `Map` 的 `key`.



### 级联查询

1. 接口和配置文件如下

   ```java
   public class User {
       private Long id;
       private String name;
       private Integer age;
       private Job job;
   }
   ```



   ```java
   User selectUserWithJob(Long id);
   ```

   ```xml
   <resultMap id="userMap" type="com.godfunc.User">
       <result column="id" property="id"/>
       <result column="name" property="name"/>
       <result column="age" property="age"/>
       <result column="job_id" property="job.id"/>
       <result column="job_name" property="job.name"/>
       <result column="u_id" property="job.uid"/>
   </resultMap>
   <select id="selectUserWithJob" resultMap="userMap">
       select u.*, b.id job_id, b.name job_name, b.u_id  from user u left join job b on b.u_id = u.id where u.id = #{id}
   </select>
   ```

   这里是一个对一关联，也可以使用 `association` 标签做，但是这里直接使用了 `.` 的方式。



### 子查询

1. 当我们进行子查询，想要传递多个参数时，可以这样做 `column="{key1=column1,key2=column2}"`* 
2. `fetchType="eager|lazy"`  设置子查询立即加载|懒加载

3. `bind` 将一个表达式的值绑定到一个变量中（可以用来做模糊查询的`%` 拼接）

   ```xml
   <select id="selectByUserName" resultType="com.godfunc.User">
       <bind name="_name" value="'%'+name+'%'"/>
       select * from user where name like #{_name}
   </select>
   ```

   *Note: 如果只有一个参数，也要加 `@Param` 注解，不然会报错*



### 缓存

1. 一级缓存 默认是开启的
2. 二级缓存
   1. 缓存的回收策略（eviction）：
      - LRU - 最近最少使用的 ：移除最长时间不被使用的对象。
      - FIFO - 先进先出：按对象进入缓存的顺序来移除他们。
      - SOFT - 软引用：移除基于垃圾回收器状态和软引用规则的对象。
      - WEAK - 弱引用：更积极地移除基于垃圾收集器状态和弱引用规则的对象。
   2. 缓存刷新间隔（flushInterval）：缓存多久时间清空一次，默认不清空，设置一个毫秒值。
   3. 是否只读（readOnly）

