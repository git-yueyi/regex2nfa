package com.yueyi.laxter;

import java.util.Iterator;
import java.util.List;
import java.util.Stack;

public class App {

	/**
	 * Basic grammars: Empty: S -> ϵ Cat: S -> S S Or: S -> S | S Star: S -> S *
	 * 大括号: S -> S{num1,num2}  小括号 S -> ( S )
	 *
	 * Extension: Plus: S -> S + -> S S * Ques: S -> S ? -> (S | ϵ) 方括号: S ->
	 * [SS..]->S|S|.. S -> [^SS..]->
	 */

	public static void main(String[] args) {

		String inRegex = "c(a|b)";
		//String inRegex = "((aa|bb)|((ab|ba)(aa|bb)*(ab|ba)))*";

		Node nfa = regex2NFA(inRegex, 0, inRegex.length());
		System.out.println(nfa);
	}

        /***
	 * 在正则表达式里，表达式与操作符是右结合的，
	 * 如：a+, 然后两个表达式之间要么是是concat组合，要么是或组合，
	 * 所以我们在构造语法树时，可以考虑从右往左，依次将各个小的表达式，
	 * 操作符分别抽出来，然后对该小的正则表达式构建语法树则可。
	 * @param inRegex
	 * @param begin
	 * @param end
	 * @return
	 */
	private static Node regex2NFA(String inRegex, int begin, int end) {
		int stkflg = 0;
		int starti = -1;
		int endi = -1;
		Stack<Node> stack = new Stack<Node>();
		for (int i = begin; i < end; i++) {
			char c = inRegex.charAt(i);

			switch (c) {
			case '|':
				
				Node node1 = concatStack(stack);//regex2NFA(inRegex, begin, i);
				stack.clear();
				
				Node node2 = regex2NFA(inRegex, i + 1, end);

				Node nodeOrHead = new Node();
				Node nodeOrTail = new Node();

				node1.addTail(Node.EPSINO, nodeOrTail);
				node2.addTail(Node.EPSINO, nodeOrTail);

				nodeOrHead.headLink(Node.EPSINO, node1);
				nodeOrHead.headLink(Node.EPSINO, node2);
				nodeOrHead.setTail(nodeOrTail);
				return nodeOrHead;
			case '(':
				starti = i;
				stkflg += 1;
				while (stkflg != 0) {
					c = inRegex.charAt(++i);
					switch (c) {
					case '(':
						stkflg += 1;
						break;
					case ')':
						stkflg -= 1;
						break;
					default:
						break;
					}
				}
				endi = i;
				Node regex2nfa = regex2NFA(inRegex, starti + 1, endi);
				Node concatStack2 = concatStack(stack);
				stack.clear();
				if(concatStack2!=null)
					stack.push(concatStack2);
				stack.push(regex2nfa);
				
				break;
			case '*':
				Node pop = stack.pop();
				Node n1 = new Node();
				Node n2 = new Node();
				n1.headLink(Node.EPSINO, pop);
				pop.getTail().getEdges().add(new Edge(Node.EPSINO, pop.getHead()));
				pop.addTail(Node.EPSINO, n2);
				n1.setTail(n2);
				n1.getEdges().add(new Edge(Node.EPSINO, n2));
				stack.push(n1);
				break;
			case '{':
				int commai  = 0;
				starti = i;
				while(inRegex.charAt(++i)!='}') {
					if(inRegex.charAt(i)==',') {
						commai = i;
					}
				}
				endi = i;
				String num1str = inRegex.substring(starti+1, commai);
				String num2str = null;
				if(endi> commai+1)
					num2str = inRegex.substring(commai+1, endi);
				starti = 0; endi=0;
				
				int num1 = Integer.parseInt(num1str);
				int num2 = 0;
				if (num2str !=null) {
					num2 = Integer.parseInt(num2str);
				}
				Node pop2 = stack.pop();
				Node node3 = new Node();
				Node lastnode  = new Node();
				lastnode.copy(pop2);
				for (int j = 1; j < (num2>0?num2:num1); j++) {
					Node node = new Node();
					node.copy(lastnode);
					pop2.concat(node);
					if(j+1<num1) {
						continue;
					}
					node.getTail().getEdges().add(new Edge(Node.EPSINO,node3 ));
				}
				pop2.setTail(node3);
				stack.push(pop2);
				
				break;
			default:
				Node nodeh = new Node();
				Node nodet = new Node();
				nodeh.addTail(c + "", nodet);

				Node concatStack = concatStack(stack);
				stack.clear();
				if(concatStack!=null)
					stack.push(concatStack);
				stack.push(nodeh);
				
				break;
			}

		}
		Node concatStack = concatStack(stack);
		return concatStack;
		
		 
}

	private static Node concatStack(Stack<Node> stack) {
		Node node = null;
		for (int i=0; i<stack.size() && stack.get(i)!=null;i++) {
			if(i==0 )
				node = stack.get(i);
			else 
				node.concat(stack.get(i));
		}
		return node;
	}
}
