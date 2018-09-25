
## 配置

```项目的gradle
apply plugin: 'com.yy.lib.analysis'

buildscript {
    repositories {
        // ...
       maven {
            url uri('D:\\code\\release')//本地插件位置
        }
    }
    dependencies {
          classpath 'com.yy.plugin:library-analysis:1.0'//依赖
    }
}

在app build.gradle中如下引入：
apply plugin: 'com.yy.lib.analysis'

libReport {
    output = [
        "txt", "html" // default
    ]
    ignore = [
        "com.android.support:support-v4"
    ]
}
```

## 使用

```
gradle depLibReportCompile // or depLReportReleaseCompileClasspath ...
```

注意：在使用gradle 4.x以后，建议使用libReportReleaseCompileClasspath输出（libReportCompile无法输出使用implementation加入的相关依赖库），如果工程都只用compile添加依赖，那么没有影响。
若一个项目中有多个module，且被主程序引用，需要先注销project()，取消他们的依赖，然后逐个进行扫描。

## 插件生成
在task点击uploadArchives 执行生成
发布配置：
```
//设置maven deployer
uploadArchives {
    repositories {
        mavenDeployer {
            pom.groupId = 'com.yy.plugin' //唯一标识符
            pom.artifactId = 'library-analysis'
            pom.version = 1.0
            //文件发布到下面目录。
            //此处发布到本地maven仓库 ，如要发布到jcenter，方法相同。
            repository(url: uri('../release'))
        }
    }
}
```