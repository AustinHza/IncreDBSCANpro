
1.第一版本是简单的两次DBSCAN合并
2.第二次改动可以自动寻找radius值，但效果不好
3.第三版改动增加了四叉树和包围盒
4.第四版加了knn寻找radius，但没什么效果，代码仍然保留在其中
5.第五版改为调用增量dbscan之前才计算参数，而不是一读取数据就计算
6.第六版的改动，首先是补充了当船只状态为lost时的相关处理，读取数据时按时间流读取，当所有船只遍历检查时，如果在规定时间段内
没有再收到新的更新数据，则将其标为lost，并将该数据作为满足出口点的对象来处理
目前这是第6版
