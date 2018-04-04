import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

/**
 * 创建相关实体类
 * Created by eightpigs on 2016/11/19.
 * Updated on 2018/04/04.
 *
 *
 * 使用说明：
 *  1. 请主要查看静态变量的配置项
 *  2. 根据配置项说明进行配置
 *  3. 配置完成运行类并查看效果
 */
public class InitEntity {

    /**
     * 数据库名
     */
    private static String dbName = "dbName";

    /**
     * 连接字符串
     * 其中 useInformationSchema=true 参数必须加,不然获取不到表的注释
     */
    private static String connStr = "jdbc:mysql://host:port/"+ dbName +"?useInformationSchema=true&serverTimezone=UTC&useUnicode=true&characterEncoding=utf8&autoReconnect=true";

    /**
     * 连接对象
     */
    private static Connection conn;

    /**
     * 数据库连接用户
     */
    private static String connUser = "";

    /**
     * 数据库连接密码
     */
    private static String connPass = "";

    /**
     * 源代码路径
     */
    public static String classFilePath = System.getProperty("user.dir") + "/src/main/java/";

    /**
     * 包名
     */
    private static String packageName = "me.lyinlong.dao.entity";

    /**
     * 表前缀(生成过程中会将此字符串删除 , 没有请填为空字符串)
     */
    private static String tablePrefix = "";

    /**
     * 类注释上的创建用户
     */
    private static String tableAuthor = "eightpigs";

    /**
     * 创建时间
     */
    public static String time = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

    /**
     * 是否生成一个无參+一个全參构造
     */
    public static Boolean genConstructor = true;

    /**
     * 将含有以下关键字的表归类到一个包中
     */
    public static List<String> packageGroups = new ArrayList<String>(){{
        add("product");
        add("account");
        add("order");
    }};

    /**
     * 属性名称是否转换为驼峰命名(如果有下划线的话）
     */
    public static Boolean CamelNamed = true;



    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        // 加载数据库驱动
        Class.forName("com.mysql.jdbc.Driver");
        // 连接
        conn = DriverManager.getConnection(connStr, connUser, connPass);
        // 获取该数据库中所有的表名
        ArrayList<Table> tables = getTables();
        for (Table table : tables) {

            // 生成类名
            table.setClassName(null , tablePrefix);

            // 设置包名
            table.setPackageName(packageName);

            System.out.println("正在创建 " + table.getPackageName()+"."+table.getClassName() + " 类 [ "+ table.getRemark() +" ]");

            // 设置用户
            table.setAuthor(tableAuthor);
            // 创建类文件
            table.createObj();
        }
        conn.close();
    }

    /**获取数据库中所有表
     * @return  所有表及字段
     * @throws SQLException sql执行异常
     */
    private static ArrayList<Table> getTables() throws SQLException {
        // 获取数据库的综合信息
        DatabaseMetaData databaseMetaData = conn.getMetaData();
        // 获取所有的表
        ResultSet rsT = databaseMetaData.getTables(dbName, null, "%", null);

        ArrayList<Table> tables = new ArrayList<>();

        while (rsT.next()) {

            // 表对象
            Table table = new Table(rsT.getString("TABLE_NAME") , new LinkedHashSet<>(), rsT.getString("REMARKS") );

            // 表的所有列
            ResultSet rsC = databaseMetaData.getColumns(dbName, "%", table.getName() , "%");

            // 初始化导包集合
            table.setImports(new ArrayList<>());

            while(rsC.next()){
                Column column = new Column(rsC.getString("COLUMN_NAME") , rsC.getString("TYPE_NAME") , null ,rsC.getString("REMARKS"));
                // 将数据库的类型转换为java对象类型并且返回需要导入的包名
                table.getImports().addAll(column.transferType());
                table.getColumns().add(column);
            }
            tables.add(table);
        }
        return tables;
    }
}

/**
 * 数据库中的表
 */
class Table {
    /**
     * 表名
     */
    private String name;
    /**
     * 表中的所有列
     */
    private Set<Column> columns;
    /**
     * 表的描述
     */
    private String remark;

    /**
     * 表的类名
     */
    private String className;

    /**
     * 包名
     */
    private String packageName;
    /**
     * 创建用户
     */
    private String author;

    /**
     * 需要导入的包
     */
    private List<String> imports;

    public Table() { }

    Table(String name, Set<Column> columns, String remark) {
        this.name = name;
        this.columns = columns;
        this.remark = remark;
    }

    String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    Set<Column> getColumns() {
        return columns;
    }

    public void setColumns(Set<Column> columns) {
        this.columns = columns;
    }

    String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    String getClassName() {
        return className;
    }

    public String getAuthor() {
        return author;
    }

    void setAuthor(String author) {
        this.author = author;
    }

    void setClassName(String className, String prefix) {
        className = className == null ? name : className;
        this.className = TextUtils.getFirstUpperName(TextUtils.delUnderlined(className.replace(prefix,"")));
    }

    public void setClassName(String className) {
        this.className = className;
    }

    String getPackageName() {
        return packageName;
    }

    void setPackageName(String packageName) {
        this.packageName = packageName;
        // 判断是否需要归类
        for (String pg : InitEntity.packageGroups) {
            if(name.contains(pg) && className.toLowerCase().contains(pg) && className.toLowerCase().indexOf(pg) == 0){
                this.packageName += ("."+pg);
            }
        }
    }

    List<String> getImports() {
        return imports;
    }

    void setImports(List<String> imports) {
        this.imports = imports;
    }

    /**
     * 创建类文件(name.java)
     */
    void createObj(){

        StringBuilder builder = new StringBuilder();
        // 生成包名
        if(packageName != null && packageName.length() > 0){
            builder.append("package ");
            builder.append(packageName);
            builder.append(";\n\n");
        }

        // 导包
        if(imports != null && imports.size() > 0){
            // 去重
            imports = new ArrayList<>(new HashSet<>(imports));

            for (String p : imports) {
                builder.append("import ");
                builder.append(p);
                builder.append(";\n");
            }
            builder.append("\n");
        }

        // 类的注释
        if(remark != null && remark.length() > 0){
            builder.append("/**\n");
            builder.append(" * ");
            builder.append(remark);
            builder.append("\n");
            builder.append(" * Created by ");
            builder.append(author);
            builder.append(" on ");
            builder.append(InitEntity.time);
            builder.append(".");
            builder.append("\n */\n");
        }

        builder.append("public class ");
        builder.append(className);
        builder.append(" {\n\n");

        // 生成属性
        for (Column column : columns) {
            column.setAttrName(null);

            // 如果有注释,生成注释片段
            if(column.getRemark() != null && column.getRemark().length() > 0){
                builder.append("\t/**\n");
                builder.append("\t * ");
                builder.append(column.getRemark());
                builder.append("\n\t */\n");
            }
            builder.append("\tprivate ");
            builder.append(column.getTypeClassName());
            builder.append(" ");
            builder.append(getAttrName(column.getAttrName()));
            builder.append(";\n\n");
        }

        // 生成无參 + 全參构造
        if(InitEntity.genConstructor){
            // 无參
            builder.append("\tpublic ");
            builder.append(className);
            builder.append("() { }\n\n");

            // 全參
            builder.append("\tpublic ");
            builder.append(className);
            builder.append("(");
            int i = 0 ;
            for (Column column : columns) {
                if (i++ > 0) {
                    builder.append(",");
                }
                builder.append(column.getTypeClassName());
                builder.append(" ");
                builder.append(getAttrName(column.getAttrName()));
            }
            builder.append(") {\n");
            for (Column column : columns) {
                builder.append("\t\tthis.");
                builder.append(getAttrName(column.getAttrName()));
                builder.append(" = ");
                builder.append(getAttrName(column.getAttrName()));
                builder.append(";\n");
            }
            builder.append("\t}\n");
        }

        // 生成getter/setter
        for (Column column : columns) {
            // 生成getter
            builder.append("\n\tpublic ");
            builder.append(column.getTypeClassName());
            builder.append(" get");
            builder.append(TextUtils.getFirstUpperName(getAttrName(column.getName())));
            builder.append("() {\n");
            builder.append("\t\treturn ");
            builder.append(getAttrName(column.getAttrName()));
            builder.append(";\n\t}\n");

            // ------------------------------------------

            // 生成setter
            builder.append("\n\tpublic void set");
            builder.append(TextUtils.getFirstUpperName(getAttrName(column.getName())));
            // 方法参数
            builder.append("(");
            builder.append(column.getTypeClassName());
            builder.append(" ");
            builder.append(getAttrName(column.getAttrName()));
            builder.append(") ");
            builder.append("{\n");
            builder.append("\t\tthis.");
            builder.append(getAttrName(column.getAttrName()));
            builder.append(" = ");
            builder.append(getAttrName(column.getAttrName()));
            builder.append(";\n\t}\n");
        }

        builder.append("\n}");
        String filePath = InitEntity.classFilePath + (packageName.replace(".","/")) + "/" + className +".java";
        // 创建类
        FileUtils.writeFile(builder.toString() , filePath);

    }

    /**
     * 根据配置获取是否转换为驼峰命名的属性名
     * @param name  字段名
     * @return  属性名
     */
    private String getAttrName(String name){
        StringBuilder nameBuilder;
        if(InitEntity.CamelNamed && name.contains("_")){
                nameBuilder = new StringBuilder();
                String[] names = name.split("_");
                nameBuilder.append(names[0]);
                for (int i = 1; i < names.length; i++) {
                   nameBuilder.append(String.valueOf(names[i].charAt(0)).toUpperCase() + names[i].substring(1));
                }
                return nameBuilder.toString();
            }
            return name;
    }
}

/**
 * 数据库中的列
 */
class Column{
    /**
     * 列名
     */
    private String name;
    /**
     * 列在数据库中的类型
     */
    private String databaseType;
    /**
     * 对应的java完全限定名
     */
    private String typeClassName;
    /**
     * 列的描述
     */
    private String remark;

    /**
     * 在类中的属性名(通过name属性进行转换后的值)
     */
    private String attrName;

    /**
     * 将数据库中常用的数据类型转换为java 全限定名
     * @return 返回需要导的包
     */
    List<String> transferType(){

        // 删除不需要的类型字符串
        String tempTypename = databaseType.replace("UNSIGNED","");

        // 需要导的包
        List<String> imports = new ArrayList<>();

        switch (tempTypename) {
            case "CHAR":
            case "VARCHAR":
            case "LONGVARCHAR":
                typeClassName = "String";
                break;
            case "NUMERIC":
            case "DECIMAL":
                typeClassName = "BigDecimal";
                // 添加包
                imports.add("java.math.BigDecimal");
                break;
            case "INT":
            case "INT UNSIGNED":
            case "TINYINT":
            case "SMALLINT":
            case "INTEGER":
                typeClassName = "Integer";
                break;
            case "BIT":
                typeClassName = "Boolean";
                break;
            case "BIGINT":
                typeClassName = "Long";
                break;
            case "REAL":
                typeClassName = "Float";
                break;
            case "FLOAT":
            case "DOUBLE":
                typeClassName = "Double";
                break;
            case "BINARY":
            case "VARBINARY":
            case "LONGVARBINARY":
                typeClassName = "byte[]";
                break;
            case "DATE":
            case "DATETIME":
            case "TIME":
                typeClassName = "Date";
                imports.add("java.util.Date");
                break;
            case "TIMESTAMP":
                typeClassName = "Timestamp";
                imports.add("java.sql.Timestamp");
                break;
            default:
                typeClassName = "String";
                break;
        }
        return imports;
    }

    public Column() { }

    Column(String name, String databaseType, String typeClassName, String remark) {
        this.name = name;
        this.databaseType = databaseType;
        this.typeClassName = typeClassName;
        this.remark = remark;
    }

    String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDatabaseType() {
        return databaseType;
    }

    public void setDatabaseType(String databaseType) {
        this.databaseType = databaseType;
    }

    String getTypeClassName() {
        return typeClassName;
    }

    public void setTypeClassName(String typeClassName) {
        this.typeClassName = typeClassName;
    }

    String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    String getAttrName() {
        return attrName;
    }

    /**
     * 将name值进行转换为正常的属性名
     * @param attrName
     */
    void setAttrName(String attrName) {
        this.attrName = TextUtils.delUnderlined(name);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}

/**
 * 文本字符操作类
 */
class TextUtils{

    /**
     * 首字母大写
     * @param name
     * @return
     */
    static String getFirstUpperName(String name){
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    /**
     * 删除下划线并将下划线后的字符转为大写
     * @param text
     * @return
     */
    static String delUnderlined(String text){
        // 所有下划线
        char[] valChars = text.toCharArray();
        // 将所有下划线的下标保存
        for (int i = 0; i < valChars.length; i++) {
            if(valChars[i] == '_'){
                // 如果下划线在第一个或者最后一个
                if(i == 0 || i == valChars.length - 1) {
                    valChars[i] = ' ';
                } else { // 将_后面的字符转为大写
                    valChars[i+1] = String.valueOf(valChars[i+1]).toUpperCase().toCharArray()[0];
                }
            }
        }
        return String.valueOf(valChars).trim().replace("_","");
    }
}

/**
 * 文件操作类
 */
class FileUtils {
    /**
     * 写入文件
     * @param content   要写入的内容
     * @param path      路径
     */
    static void writeFile(String content, String path){
        try {
            // 目录
            String dirPath = path.substring(0,path.lastIndexOf("/"));
            File dir = new File(dirPath);
            if(!dir.exists()){
                dir.mkdirs();
            }
            // 文件
            File file = new File(path);
            if(file.exists()){
                file.delete();
            }
            file.createNewFile();
            BufferedWriter out = new BufferedWriter(new FileWriter(file));
            out.write(content);
            out.flush();
            out.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
