#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <malloc.h>
#define  MAXRESERVED 22 
enum TokenType{    LPAREN, RPAREN, LSQBRAC, RSQBRAC, IDENT, NUMBER,  ASSIGN, LT, LQ, BT, BQ, EQ,
    SEMICOLON, LCURLYBRACE, RCURLYBRACE, IF, THEN,ELSE, END,READ,WRITE,
    REPEAT, UNTIL,COMMENTS,LOGICJUDG ,BITOR,BITAND,OR, AND,ADD,SUB,MUL,DIVI,};
enum PARSESTAT{START,ASSIGNING, IDENTING,PARENING,NOTING,NUMBERING,INOR,INAND, LOGICJUDGING, STOP} ;                  
struct Token{
    char  content[1024];
    int type;
}reservedWords[MAXRESERVED]
= {{"if",IF},{"then",THEN},{"else",ELSE},{"end",END},
    {"repeat",REPEAT},{"until",UNTIL},{"read",READ},
    {"write",WRITE},{"(",LPAREN},{")",RPAREN},{"+",ADD},
    {"-",SUB},{"*",MUL},{"/",DIVI},{"[",LSQBRAC},{"]",RSQBRAC},
    {"<",LT},{"<=",LQ},{">",BT},{">=",BQ},{"=",EQ},{";",SEMICOLON}};

/* lookup an identifier to see if it is a reserved word */
/* uses linear search */
int reservedLookup (char * s)
{ int i;
    for (i=0;i<MAXRESERVED;i++)
        if (!strcmp(s,reservedWords[i].content))
            return reservedWords[i].type;
    return IDENT;
}
struct LinkedListToken{
    struct Token tok;
    struct LinkedListToken * next;
    char  errInfo[128];
};
int appendLinkList(struct LinkedListToken * pCur,struct LinkedListToken ** pnode){
    *pnode = (struct LinkedListToken *)malloc(sizeof(struct LinkedListToken));
    (*pnode)->next = NULL;
    pCur->next = *pnode;
    return 0;
};
int addToken(struct LinkedListToken * pCur,struct LinkedListToken ** pnode, char ch, int i, int tp){
    appendLinkList(pCur, pnode);
    pCur->next->tok.content[i] = ch;
    pCur->next->tok.type=tp;
    return i+1;
};


long pos = 0l;
void findToken(FILE * fp){

    int i = 0;
    int tidx = 0;
    char ch = '\0';
    int stat = START; 

    struct LinkedListToken * pHead = (struct LinkedListToken *)malloc(sizeof(struct LinkedListToken));
    struct LinkedListToken * pnode = NULL;
    struct LinkedListToken * pCur = pHead;

    while(ch != EOF ){
        if(stat!=STOP){ 
            ch = fgetc(fp); 
            pos++;
        }
        switch(stat){
            case START:
                if((ch>='a' && ch<='z' )||(ch>='A' && ch <='Z')||ch=='_'){
                    i = addToken(pCur,&pnode,ch,i,IDENT);
                    stat = IDENTING;
                }else if(ch=='{') {
                    i = addToken(pCur,&pnode,ch,i,COMMENTS);
                    stat = NOTING; 
                }else if(ch>='0' && ch<='9'){
                    i = addToken(pCur,&pnode,ch,i,NUMBER);
                    stat = NUMBERING;
                }else if(ch=='>'|| ch=='<' ||ch=='=' ) {
                    i = addToken(pCur,&pnode,ch,i,LOGICJUDG);
                    stat = LOGICJUDGING;
                }else if(ch==':'){
                    i = addToken(pCur,&pnode,ch,i,ASSIGN);
                    stat = ASSIGNING;
                }else if(ch=='|'){
                    i = addToken(pCur,&pnode,ch,i,OR);
                    stat = INOR;
                }else if(ch=='&'){
                    i = addToken(pCur,&pnode,ch,i,AND);
                    stat = INAND;
                }else if(ch=='(' || ch==')' || ch=='*' || ch=='/' || ch=='+'|| ch=='-'|| ch=='['|| ch==']'||ch==';'){ 
                    i = addToken(pCur,&pnode,ch,i,IDENT);
                    stat = STOP;
                }
                break;
            case IDENTING:
                if((ch>='0' && ch <= '9')||(ch>='a' && ch<='z' )||(ch>='A' && ch <='Z')||ch=='_'){
                    pCur->next->tok.content[i++] = ch;
                    stat = IDENTING;
                }else{
                    fseek(fp,--pos,0);
                    stat = STOP;
                }
                break;
            case NOTING:
                pCur->next->tok.content[i++] = ch;
                if(ch =='}'){
                    stat = STOP;
                }
                break;
            case NUMBERING:
                if(ch>='0' && ch<='9'){
                    pCur->next->tok.content[i++] = ch;
                    stat = NUMBERING;
                }else{
                    fseek(fp,--pos,0);
                    stat = STOP;
                }
                break;
            case LOGICJUDGING:
                if(ch=='='){
                    pCur->next->tok.content[i++] = ch;
                }else{
                    fseek(fp,--pos,0);
                }
                stat = STOP;
                break;
            case ASSIGNING:
                if(ch=='='){
                    pCur->next->tok.content[i++] = ch;
                }else{
                    sprintf( pCur->next->errInfo,"expected '=' after ':' at location:%d",pos);
                    fseek(fp,--pos,0);
                }
                stat = STOP;
                break;
            case INOR:
                if(ch=='|'){
                    pCur->next->tok.content[i++] = ch;
                }else{
                    pCur->next->tok.type = BITOR;
                    fseek(fp,--pos,0);
                }
                stat = STOP;
                break;
            case INAND:
                if(ch=='&'){
                    pCur->next->tok.content[i++] = ch;
                }else{
                    pCur->next->tok.type = BITAND;
                    fseek(fp,--pos,0);
                }
                stat = STOP;
                break;
            case STOP:
                pCur->next->tok.content[i]='\0';
                pCur = pCur->next;

                if(pCur->tok.type == IDENT || pCur->tok.type ==LOGICJUDG){
                    pCur->tok.type =  reservedLookup (pCur->tok.content);
                }

                i=0;
                stat = START;
            default:
                break;
        }
    }
    // print out all of the tokens
    i=0;
    pCur = pHead->next;
    while(pCur!=NULL){
        if(strlen(pCur->errInfo)>1)
            printf("node[%d]==>type=>%d cont=>%s , errInfo: %s\n",i,pCur->tok.type,pCur->tok.content ,pCur->errInfo);
        else
            printf("node[%d]==>type=>%d cont=>%s \n",i,pCur->tok.type,pCur->tok.content );
        pnode = pCur;
        pCur = pCur->next;
        free(pnode);
        i++;
    }

}

int main(void){

    FILE *fp;
    char ch = EOF;
    if((fp=fopen("test.tiny","r+"))==NULL)
    {
        printf("open filed !");
        return 1;
    }
    findToken(fp);
    printf("Done!");
    return 0;
}
