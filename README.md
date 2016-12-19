# PercentView
其实很早就打算试着写一些自定义view的东西，但是总有种太复杂的不会，太简单的不想写。可能这也是程序员的通病吧，既然是病，那就得改，不怕程序简单，就怕你不写，共勉。今天这个demo就是我自己写的一个自定义percentview，效果不是很炫，通用性也不是很强（好吧 我自己都鄙视了自己一下），主要是用于学习这个流程。

好了，废话不多说，直接上效果图：

![效果图](http://img.blog.csdn.net/20161219143015670?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvdTAxMzMyMDg2OA==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)
 
 使用方法和普通的view是一样的，如下：
  

```
  <com.example.xiaoshuai.percentview.PercentView
       android:layout_width="300dp"
       android:layout_height="300dp"
       android:layout_centerInParent="true"
       app:color_win="#ff0000"
       app:color_dogfall="#00ff00"
       app:color_lose="#0000ff"
       app:win_percent="20"
       app:lose_percent="30"
       app:dogfall_percent="50"
       app:is_startAnimator="true"
       />  
```

 自定义属性上面都使用了，而且比较简单，就不做介绍了，感兴趣的话，可以看一下代码。
 欢迎大家指教
 /*******************结束分割线*******************/