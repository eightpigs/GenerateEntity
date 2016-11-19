# GenerateEntity
## 将mysql数据库中表生成实体类

---
### 使用
> 1. 没有将此功能单独写一个项目 , 可以将InitEntity类复制到自己项目中(确保有mysql驱动) ,直接运行main方法就行

---

### 功能
> 1. 自动根据数据库中表结构的注释生成java类/属性的注释
> 2. 支持选择性生成构造
> 3. 支持指定包名 / 作者 / 表前缀 

### 示例

表结构为:
```sql

CREATE TABLE `news` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `title` varchar(255) NOT NULL COMMENT '标题',
  `intro` varchar(255) NOT NULL COMMENT '说明(简介)',
  `content` text NOT NULL COMMENT '具体内容(富文本格式)',
  `createTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  `url` varchar(255) NOT NULL COMMENT '外链的URL',
  `status` tinyint(1) NOT NULL COMMENT '状态 1显示   2不显示',
  `seq` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='新闻资讯表';

```
包名为:
```java
me.lyinlong.test
```


生成的java实体类为:(注释都是表结构中的)

```java
package me.lyinlong.test;

import java.sql.Timestamp;

/**
 * 新闻资讯表
 * Created by eightpigs on 2016-11-19
 */
public class News {

	private Integer id;

	/**
	 * 标题
	 */
	private String title;

	/**
	 * 说明(简介)
	 */
	private String intro;

	/**
	 * 具体内容(富文本格式)
	 */
	private String content;

	/**
	 * 创建时间
	 */
	private Timestamp createTime;

	/**
	 * 外链的URL
	 */
	private String url;

	/**
	 * 状态 1显示   2不显示
	 */
	private Integer status;

	private Integer seq;

	public News() { }

	public News(Integer id,String title,String intro,String content,Timestamp createTime,String url,Integer status,Integer seq) {
		this.id = id;
		this.title = title;
		this.intro = intro;
		this.content = content;
		this.createTime = createTime;
		this.url = url;
		this.status = status;
		this.seq = seq;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getIntro() {
		return intro;
	}

	public void setIntro(String intro) {
		this.intro = intro;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Timestamp getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Integer getSeq() {
		return seq;
	}

	public void setSeq(Integer seq) {
		this.seq = seq;
	}

}
```





