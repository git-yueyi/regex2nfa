# regex2nfa
正则表达式转换为NFA，基于Thompson算法，递归构造NFA。（regex to nfa using thompson's construction. ）
如正则表达式:```c(a|b)```
输出:
0-c->1-ep->2-a->3-ep->7
0-c->1-ep->4-b->5-ep->7

其中ep为epsilon.
