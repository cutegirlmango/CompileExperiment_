package LL1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Stack;

//E->TG
//G->+TG
//G->~
//T->FH
//H->*FH
//H->~
//F->(E)
//F->i
//$
//i*i+i#
public class LL1 {
    public static void main(String[] args) {

        Scanner n = new Scanner(System.in);
        ArrayList<String> grammer = new ArrayList<>();
        MyLL1 myLL1 = new MyLL1();
        //输入文法
        System.out.println("请输入文法：（以$结尾，用～代替空串）");
        for(int i=0;true;i++) {

            String s1 = n.nextLine();
            if(i == 0){
                myLL1.setBegin(s1.charAt(0));
            }
            if(s1.equals("$")) {
                break;
            }
            grammer.add(s1);
        }
        myLL1.setInputExperssion(grammer.toArray(new String[grammer.size()]));
        //输入要判断的句子
        System.out.println("请输入句子：(以#结尾)");
        String sentence = n.nextLine();
        myLL1.setStrInput(sentence);

        myLL1.getVnVt();//求文法的终结符和非终结符
        myLL1.Init();//
        //如果预测分析表的填入过程中不产生冲突，那么该文法是LL1文法，否则该文法不是LL1文法
        if(myLL1.createTable()==0) {
            myLL1.output();
            System.out.println("文法不是LL(1)文法");
            return;
        }
        myLL1.output();
        myLL1.analyzeLL();
    }
}

class MyLL1 {
    private String[][] table;
    private String[] inputExperssion = {};
    private Stack<Character> analyzeStatck = new Stack<>();
    private String strInput = "";
    private String action = "";
    private Character begin;
    private int index = 0;
    //单个符号first集
    private HashMap<Character, HashSet<Character>> firstSet = new HashMap<>();
    //符号串first集
    private HashMap<String, HashSet<Character>> firstSetX = new HashMap<>();
    private HashMap<Character, HashSet<Character>> followSet = new HashMap<>();
    //非终结符
    private HashSet<Character> VnSet = new HashSet<>();
    //终结符
    private HashSet<Character> VtSet = new HashSet<>();
    //非终结符-产生式集合
    private HashMap<Character, ArrayList<String>> expressionSet = new HashMap<>();

    public void setInputExperssion(String[] inputExperssion) {
        this.inputExperssion = inputExperssion;
    }

    public void setStrInput(String strInput) {
        this.strInput = strInput;
    }

    public void setBegin(Character begin) {
        this.begin = begin;
    }

    //求非终结符和终结符
    public void getVnVt() {
        for (String e : inputExperssion)
            //如果是产生式左侧的，就是非终结符
            VnSet.add(e.split("->")[0].charAt(0));
        for (String e : inputExperssion)
            for (char c : e.split("->")[1].toCharArray())
                //如果不是非终结符，那么就是终结符
                if (!VnSet.contains(c))
                    VtSet.add(c);
    }
    //初始化，获取生成式并构造FIRST集和FOLLOW集
    public void Init() {
        //获取生成式
        for (String e : inputExperssion) {
            String[] str = e.split("->");
            char c = str[0].charAt(0);
            ArrayList<String> list = expressionSet.containsKey(c) ? expressionSet.get(c) : new ArrayList<>();
            list.add(str[1]);
            expressionSet.put(c, list);
        }
        //构造非终结符的first集
        for (char c : VnSet)
            getFirst(c);
        //构造开始符的follow集
        getFollow(begin);
        //构造非终结符的follow集
        for (char c : VnSet)
            getFollow(c);
    }
    //求first集
    private void getFirst(char c) {
        //如果处理过了该符号，直接返回
        if (firstSet.containsKey(c))
            return;
        //暂存集合
        HashSet<Character> set = new HashSet<>();
        // 若c属于终结符;
        if (VtSet.contains(c)) {
            set.add(c);
            firstSet.put(c, set);
            return;
        }
        // 处理其每条产生式
        for (String s : expressionSet.get(c)) {
            //如果c为~，First(c)=～
            if ("~".equals(c)) {
                set.add('~');
            } else {
                //如果c->a...，则把a加入到First(c)集中；如果c->Y....,则先求first(Y)，再把first(Y)的所有非空内容添加到First(c)中
                for (char cur : s.toCharArray()) {
                    if (!firstSet.containsKey(cur))
                        getFirst(cur);
                    HashSet<Character> curFirst = firstSet.get(cur);
                    set.addAll(curFirst);
                    //找到产生式中第1个first集不含空的非终结符，
                    //将该非终结符以及之前所有非终结符的first中的所有元素（不包括空）添加到first集中
                    if (!curFirst.contains('~'))
                        break;
                }
            }
        }
        firstSet.put(c, set);
    }
    //求产生式A->a的first(a)
    private void getFirst(String s) {
        if (firstSetX.containsKey(s))
            return;
        HashSet<Character> set = new HashSet<>();
        // 从左往右扫描该式
        int i = 0;
        while (i < s.length()) {
            char cur = s.charAt(i);
            if (!firstSet.containsKey(cur))
                getFirst(cur);
            HashSet<Character> rightSet = firstSet.get(cur);
            // 将其非空first集加入左部
            set.addAll(rightSet);
            // 若包含空串 处理下一个符号
            if (rightSet.contains('~'))
                i++;
            else
                break;
            // 若到了尾部 即所有符号的first集都包含空串 把空串加入fisrt集
            if (i == s.length()) {
                set.add('~');
            }
        }
        firstSetX.put(s, set);
    }

    private void getFollow(char c) {
        ArrayList<String> list = expressionSet.get(c);
        HashSet<Character> leftFollowSet = followSet.containsKey(c) ? followSet.get(c) : new HashSet<>();
        //如果是开始符 添加 #
        if (c == begin)
            leftFollowSet.add('#');
        //查找输入的所有产生式，添加c的后跟终结符
        for (char ch : VnSet)
            for (String s : expressionSet.get(ch))
                for (int i = 0; i < s.length(); i++)
                    if (c == s.charAt(i) && i + 1 < s.length() && VtSet.contains(s.charAt(i + 1)))
                        leftFollowSet.add(s.charAt(i + 1));
        followSet.put(c, leftFollowSet);
        //从右往左扫描处理c的每一条产生式
        for (String s : list) {
            int i = s.length() - 1;
            while (i >= 0) {
                char cur = s.charAt(i);
                //只处理非终结符  I->i(E)SL
                if (VnSet.contains(cur)) {
                    // 都按 A->αBβ 形式处理
                    //1.若β存在，把β的非空first集  加入followB
                    //2.若β不存在或者β存在且first(β)包含空串，将followA加入followB
                    String right = s.substring(i + 1);
                    HashSet<Character> rightFirstSet;
                    if(!followSet.containsKey(cur))
                        getFollow(cur);
                    HashSet<Character> curFollowSet = followSet.get(cur);
                    //如果β不存在，将followA加入followB
                    if (right.length() == 0) {
                        curFollowSet.addAll(leftFollowSet);
                    } else {//如果β存在，先找出first(β),将非空的加入followB
                        if (1 == right.length()) {
                            if (!firstSet.containsKey(right.charAt(0)))
                                getFirst(right.charAt(0));
                            rightFirstSet = firstSet.get(right.charAt(0));
                        } else {
                            if (!firstSetX.containsKey(right))
                                getFirst(right);
                            rightFirstSet = firstSetX.get(right);
                        }
                        for (char var : rightFirstSet)
                            if (var != '~')
                                curFollowSet.add(var);
                        // 若first(β)包含空串,将followA加入followB
                        if (rightFirstSet.contains('~'))
                            curFollowSet.addAll(leftFollowSet);
                    }
                    followSet.put(cur, curFollowSet);
                }
                i--;
            }
        }
    }

//    对文法G的每个产生式A->x执行第2、3步
//    对每个终结符a属于first(x),把A->x加入M[A,a]中
//    若～属于first(x),则对任何b属于follow(A)，把A->~加入M[A,b]中
//    把所有无定义的项标上出错标志
    public int createTable() {
        Object[] VtArray = VtSet.toArray();
        Object[] VnArray = VnSet.toArray();
        // 预测分析表初始化
        table = new String[VnArray.length + 1][VtArray.length + 1];
        table[0][0] = "Vn/Vt";
        //初始化首行首列
        for (int i = 0; i < VtArray.length; i++)
            table[0][i + 1] = (VtArray[i].toString().charAt(0) == '~') ? "#" : VtArray[i].toString();
        for (int i = 0; i < VnArray.length; i++)
            table[i + 1][0] = VnArray[i] + "";
        //全部置error
        for (int i = 0; i < VnArray.length; i++)
            for (int j = 0; j < VtArray.length; j++)
                table[i + 1][j + 1] = " ";
        //对于每一个非终结符，判断其每一个产生式
        for (char A : VnSet) {
            for (String s : expressionSet.get(A)) {
                if (!firstSetX.containsKey(s))
                    getFirst(s);
                HashSet<Character> set = firstSetX.get(s);
                for (char a : set)
                    //如果无冲突
                    if(find(A, a).equals(" ")||find(A, '#').equals(s))
                        //填表
                        fillIn(A, a, s);
                    else return 0;
                //如果first集中含有空串
                if (set.contains('~')) {
                    HashSet<Character> setFollow = followSet.get(A);
                    //如果follow集中含有"#"
                    if (setFollow.contains('#'))
                        //如果无冲突
                        if(find(A, '#').equals(" ")||find(A, '#').equals(s))
                            //填表
                            fillIn(A, '#', s);
                        else return 0;
                    //如果无冲突，将s填入任何属于follow集的终结符项中
                    for (char b : setFollow)
                        if(find(A, b).equals(" ")) {
                            fillIn(A, b, s);
                        }else if(find(A, '#').equals(s)){}
                        else{ return 0;}
                }
            }
        }
        return 1;
    }

    public void analyzeLL() {
        System.out.println("------------------------LL分析过程------------------------");
        System.out.println("		    符号栈	         	输入串     下一步动作使用产生式");
        //#入栈
        analyzeStatck.push('#');
        analyzeStatck.push(begin);
        //取符号栈顶
        char item = analyzeStatck.peek();
        while (item != '#') {
            char a = strInput.charAt(index);
            if (item == a) {
                action = "";
                displayLL();
                analyzeStatck.pop();
                index++;
            } else if (VtSet.contains(item)) {
                action = "";
                displayLL();
                System.out.println("不符合文法 at '" + strInput.charAt(index) + "' in " + (index+1));
                return;
            } else if (find(item, a).equals(" ")) {
                action = "";
                displayLL();
                System.out.println("不符合文法 at '" + strInput.charAt(index) + "' in " + (index+1));
                return;
            } else if (find(item, a).equals("~")) {
                action = item + "->~";
                displayLL();
                analyzeStatck.pop();
            } else {
                String str = find(item, a);
                if (str != "") {
                    action = item + "->" + str;
                    displayLL();
                    analyzeStatck.pop();
                    int len = str.length();
                    for (int i = len - 1; i >= 0; i--)
                        analyzeStatck.push(str.charAt(i));
                } else {
                    action = "";
                    displayLL();
                    System.out.println("不符合文法 at " + strInput.charAt(index) + "' in " + (index+1));
                    return;
                }
            }
            item = analyzeStatck.peek();

        }
        action="";
        displayLL();
        System.out.println("LL(1)分析成功");
        System.out.println("------------------------LL分析过程------------------------\n");
    }

    private String find(char X, char a) {
        //如果a为空串，
        if (a == '~') a = '#';
        for (int i = 1; i < VnSet.size() + 1; i++) {
            if (table[i][0].charAt(0) == X)
                for (int j = 1; j < VtSet.size() + 1; j++) {
                    if (table[0][j].charAt(0) == a)
                        return table[i][j];
                }
        }
        return "";
    }

    private void fillIn(char X, char a, String s) {
        if (a == '~') a = '#';
        for (int i = 0; i < VnSet.size() + 1; i++) {
            if (table[i][0].charAt(0) == X)
                for (int j = 0; j < VtSet.size() + 1; j++) {
                    if (table[0][j].charAt(0) == a) {
                        table[i][j] = s;
                        return;
                    }
                }
        }
    }

    private void displayLL() {
        // 输出 LL1
        Stack<Character> s = analyzeStatck;
        System.out.printf("%25s", s);
        System.out.printf("%15s", strInput.substring(index));
        System.out.printf("%13s", action);
        System.out.println();
    }

    public void output() {
        System.out.println("--------first集--------");
        for (Character c : VnSet) {
            HashSet<Character> set = firstSet.get(c);
            System.out.print("First " + c + ":[");
            for (Character var : set)
                System.out.print(var+" ");
            System.out.print("]");
            System.out.println();
        }
        System.out.println("--------first集--------\n");
        System.out.println("--------follow集--------");

        for (Character c : VnSet) {
            HashSet<Character> set = followSet.get(c);
            System.out.print("Follow " + c + ":[");
            for (Character var : set)
                System.out.print(var+" ");
            System.out.print("]");
            System.out.println();
        }
        System.out.println("--------follow集--------\n");

        System.out.println("--------------------------------------LL1预测分析表--------------------------------------");

        for (int i = 0; i < VnSet.size() + 1; i++) {
            for (int j = 0; j < VtSet.size() + 1; j++) {
                if(i==0||j==0){
                    System.out.printf("%10s", table[i][j] + "    ");
                }else {
                    if(table[i][j].equals(" ")){
                        System.out.printf("%10s", table[i][j] + " ");
                    }else {
                        System.out.printf("%10s", table[i][0]+"->"+table[i][j] + " ");
                    }

                }

            }
            System.out.println();
        }
        System.out.println("--------------------------------------LL1预测分析表--------------------------------------\n");
    }
}
