/*
** author: BowenChen
** RegexpEngine on NFA
** suuport AB,A|B,AB*,A[0-9]*B,A\*B\\,
** A[^A-Z_0-9]+,[]?
** \s == ' '
*/
public class MyNFA { 

    private Digraph G;         // digraph of epsilon transitions
    private String[] regexp;     // regular expression
    private int M;             // number of characters in regular expression
    private int start,end = -1,endPosition = -1;     // position of the start and the end of the result string

    // Create the NFA for the given RE   
    public MyNFA(String raw_regexp) {
        int N = raw_regexp.length();
        int j = 0;
        this.regexp = new String[N];                                           
        for(int i = 0; i < N; i++){                                                         //cut the regexp in to Token
            char c = raw_regexp.charAt(i);
            if(c != '[' || i > 0 && c == '[' && raw_regexp.charAt(i-1) == '\\')
                this.regexp[j++] = String.valueOf(c);
            else{
                int rp = raw_regexp.substring(i).indexOf("]") + i;
                this.regexp[j++] = raw_regexp.substring(i, rp + 1);
                i = rp;
            } 
        }
        M = j;
        Stack<Integer> ops = new Stack<Integer>(); 
        G = new Digraph(M+1); 
        for (int i = 0; i < M; i++) { 
            int lp = i;
            if(regexp[i].length() == 1)
                if (regexp[i].equals("(") || regexp[i].equals("|") && !regexp[i-1].equals("\\")) 
                    ops.push(i); 
                else if (regexp[i].equals(")")) {
                    int or = ops.pop(); 

                    // 2-way or operator
                    if (regexp[or].equals("|")) { 
                        lp = ops.pop();
                        G.addEdge(lp, or+1);
                        G.addEdge(or, i);
                    }
                    else if (regexp[or].equals("("))
                        lp = or;
                }
            if(regexp[i].equals("s") && i > 0 && regexp[i-1].equals("\\"))
                regexp[i] = " ";
            if (i < M-1 && !regexp[i].equals("\\")) {
                if (regexp[i+1].equals("*")){
                    G.addEdge(lp, i+1);
                    G.addEdge(i+1, lp);
                }
                else if (regexp[i+1].equals("+"))                                               //+, appear at leat once
                    G.addEdge(i+1, lp);
            }

            if(regexp[i].length() == 1 && (i == 0 || i > 0 && !regexp[i-1].equals("\\")))
                if (regexp[i].equals("(") || regexp[i].equals("*") || regexp[i].equals(")") || 
                    regexp[i].equals("\\") || regexp[i].equals("?") || regexp[i].equals("+"))   // ?, appear once or not at al
                    G.addEdge(i, i+1);
        } 
    } 

    // Does the NFA recognize txt? 
    public int recognizes(String txt) {
        DirectedDFS dfs = new DirectedDFS(G, 0);
        Bag<Integer> pc = new Bag<Integer>();
        //StdOut.println(G);
        for (int v = 0; v < G.V(); v++)
            if (dfs.marked(v)) pc.add(v);
        // Compute possible NFA states for txt[i+1]
        for (int i = 0; i < txt.length(); i++) {
            Bag<Integer> match = new Bag<Integer>();
            for (int v : pc) {
                if(v == M) endPosition = i;
                if(v != M){
                    if(regexp[v].equals("("))
                        start = i;
                    if(regexp[v].equals(")"))
                        end = i;
                    int l = regexp[v].length();
                    String t = String.valueOf(txt.charAt(i));
                    if (regexp[v].equals(t) || regexp[v].equals("."))
                        match.add(v+1);
                    else if(l > 1){                                                             //calculate the result of comparing [..] and txt
                        boolean contrast = false;
                        if(regexp[v].charAt(1) == '^') contrast = true;                                 //if[^], opposite the result
                        for(int j = 1; j < l; j++){
                            char c = regexp[v].charAt(j);
                            if(c == txt.charAt(i) && c != '-' || 
                                c == '-' && txt.charAt(i) >= regexp[v].charAt(j-1) && 
                                txt.charAt(i) <= regexp[v].charAt(j+1)) {
                                contrast = !contrast;
                                break;
                            }
                        }
                        if(contrast) match.add(v+1);
                    }
                }
            }
            dfs = new DirectedDFS(G, match); 
            pc = new Bag<Integer>();
            for (int v = 0; v < G.V(); v++)
                if (dfs.marked(v)) {
                    pc.add(v);
                    //StdOut.println(v);
                }
        }
        if(endPosition != -1) {
            StdOut.println(txt.substring(start, end));
            return endPosition;
        }
        return -1;
    }

    public static void main(String[] args) {
        String raw_regexp = StdIn.readLine();
        String txt = StdIn.readLine();
        
        int i = 0;
        int M = txt.length();

        while (i < M && i != -1){

            MyNFA nfa = new MyNFA(raw_regexp);
            //StdOut.println("i = " + i);
            i = nfa.recognizes(txt);
            txt = txt.substring(i);
            M = txt.length();
            //StdOut.println(txt);
        }
    }

} 

