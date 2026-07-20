grammar Flux;

tokens { INDENT, DEDENT }

@lexer::header {
  import com.yuvalshavit.antlr4.DenterHelper;
}

@lexer::members {
  private final DenterHelper denter = DenterHelper.builder()
    .nl(TERMINATOR)
    .indent(FluxParser.INDENT)
    .dedent(FluxParser.DEDENT)
    .pullToken(FluxLexer.super::nextToken);

  @Override
  public Token nextToken() {
    return denter.nextToken();
  }
}

program: declaration* precompile? (declaration | statement | terminator)* EOF ;

precompile: 'precompile' classBlock ;

declaration
    :   INDENT? ('import' | 'using') qualifiedId (wildcard=WILDCARD)? terminator? DEDENT?                           # ImportDecl
    |   INDENT? ('import' | 'using') static='static' qualifiedId (wildcard=WILDCARD)? DEDENT?                       # ImportDecl
    |   INDENT? annotation* functionModifiers
                        classWord=('class' | 'interface' | '@interface')
                        mainClass=className (((':' | 'extends')
                        extendsClass=className) | ('(' (extendsClass=className)? ')'))? ((':' | 'implements')
                        implementsClass=className)? classBlock terminator? DEDENT?                                  # ClassDecl
    |   INDENT? annotation* functionModifiers
                        VOID ID '(' formalParameters? ')' terminator? voidBlock terminator? DEDENT?                 # RunnableFunctionDecl
    |   INDENT? annotation* functionModifiers
                        VAR? ID '(' formalParameters? ')' terminator? returnBlock            terminator? DEDENT?    # VarFunctionDecl
    |   INDENT? annotation* functionModifiers
                        type ID '(' formalParameters? ')' terminator? returnBlock            terminator? DEDENT?    # ConsumerFunctionDecl

    |   INDENT? variableModifiers localVarDecl terminator? DEDENT?                                                  # VarDecl
    ;

className
    : <assoc=right> className '<' className? '>'
    | qualifiedId
    ;

type
    : <assoc=right> type '<' type? (',' type)* '>'
    | <assoc=right> type '<' type '<' type? (',' type)* '>>'
    | <assoc=right> type '<' type '<' type '<' type? (',' type)* '>>>'
    | <assoc=right> type '<' type '<' type '<' type '<' type? (',' type)* '>>>>'
    | type '[]' ('[]')*
    | VAR
    | ID
    ;

terminator : TERMINATOR+ ;

variableModifiers
    :
    accessModifier=('public' | 'protected' | 'private')?
    unfinishedMd=('unfinished' | 'illegal' | 'suboptimal' | 'unpolished' | 'ugly')?
    staticMd='static'?
    finalMd='final'?
    transientMd='transient'?
    volatileMd='volatile'?
    ;

localVarDecl
    :   var=VAR? packedId '=' expression                             # LooselyTypedLocalVar
    |   type packedId ('=' expression)?                              # StrictlyTypedLocalVar
    ;

functionModifiers
    :
    accessModifier=('public' | 'protected' | 'private')?
    unfinishedMd=('unfinished' | 'illegal' | 'suboptimal' | 'unpolished' | 'ugly')?
    implementationModifier=('abstract' | 'static' | 'native')?
    finalMd='final'?
    transientMd='transient'?
    volatileMd='volatile'?
    synchronizedMd='synchronized'?
    nativeMd='native'?
    strictfpMd='strictfp'?
    ;

annotation
    : '@' expression ('(' expressionList ')')? terminator?
    ;

formalParameters
    :   formalParameter (',' formalParameter)*
    ;

formalParameter
    :   annotation* type? packedId
    |   annotation* packedId ':' type
    ;

 // Added these two bad boys because it's funny
FIGURE_BRACKET_L : '<%' | '{' ;
FIGURE_BRACKET_R : '%>' | '}' ;

classBlock
    : FIGURE_BRACKET_L INDENT? classLines DEDENT? FIGURE_BRACKET_R
    | ':' INDENT classLines DEDENT
    ;
voidBlock
    : FIGURE_BRACKET_L INDENT? voidLines DEDENT? FIGURE_BRACKET_R
    | ':' INDENT voidLines DEDENT
    ;
returnBlock
    : FIGURE_BRACKET_L INDENT? expressionLines DEDENT? FIGURE_BRACKET_R
    | ':' INDENT expressionLines DEDENT
    ;

classLines : (declaration | statement | terminator)*;
voidLines : (statement | voidReturn | terminator)*;
expressionLines : (statement | expressionReturn | terminator)*;

lines
    :   voidLines
    |   classLines
    |   expressionLines
    ;

block
    :   voidBlock                                               # VoidBlockOption
    |   returnBlock                                             # ReturnBlockOption
    ;

expressionReturn : 'return' expression terminator? ;
voidReturn : 'return' terminator? ;

statement
    :   INDENT? declaration terminator?  DEDENT?                                           # DeclStatement
    |   INDENT? voidBlock terminator?  DEDENT?                                             # VoidBlockStatement
    |   INDENT? 'for' '(' localVarDecl terminator expression terminator assignmentStat ')'  DEDENT? block # ForStatement
    |   INDENT? 'for' localVarDecl terminator expression terminator assignmentStat  DEDENT? block # ForStatement
    |   INDENT? ('for' | 'foreach') '(' type? packedId (':' | 'in') expression ')'  DEDENT? block # ForeachStatement
    |   INDENT? ('for' | 'foreach') type? packedId (':' | 'in') expression  DEDENT? block # ForeachStatement
    |   INDENT? 'if' expression DEDENT? block terminator? (('else if' | 'elif') expression block)* terminator? ('else' else=block)? # IfStatement
    |   INDENT? assignmentStat terminator?  DEDENT?                              # AssignmentStatement
    |   INDENT? expression terminator?  DEDENT?                                  # ExpressionStatement
    |   INDENT? expressionReturn  DEDENT?                                       # ExpressionReturnStatement
    |   INDENT? voidReturn  DEDENT?                                             # VoidReturnStatement
    ;

assignmentStat
    :   as=expression operator=
    ( '='   | '+='  | '-='  | '*='
    | '/='  | '%='  | '&='  | '^='
    | '|='  | '<<=' | '>>=' | '>>>='
    ) value=expression                                                # DefaultAssigmnent
    |   as=expression operator='**=' value=expression                   # ExpAssigmnent
    |   as=expression operator='***=' value=expression                  # TetrAssigmnent
    |   as=expression operator='/%=' value=expression                   # FloorDivAssigmnent
    |   as=expression operator='%/=' value=expression                   # CeilDivAssigmnent
    |   as=expression operator=('++' | '--')                      # UnaryAssigmnent
    ;

generator_for
    : 'for' iterId=packedId ('in' | ':') collection=expression
    ;

fstring
    : 'f' FSTRING_OPENING expression? (FSTRING_MIDDLE*? expression)*? FSTRING_CLOSING
    ;

FSTRING_OPENING : '"' FSTRING_SYMBOL*? FIGURE_BRACKET_L ;

FSTRING_MIDDLE : FIGURE_BRACKET_R FSTRING_SYMBOL*? FIGURE_BRACKET_L ;

FSTRING_CLOSING : FIGURE_BRACKET_R FSTRING_SYMBOL*? '"' ;

expression
    // Please don't ask me about tetration, it only got here because it needed
    // a custom way of handling it, useful for many other future features
    : <assoc=right>  expression '***' expression                # TetrExpr
    | <assoc=right>  expression '**' expression                 # ExpExpr
    |   collection=expression '[' element=expression ']'        # ArrayAccessExpr
    | <assoc=right>  'lambda' (idList) ':' expression                        # LambdaExpr
    | <assoc=right>  '(' (idList) ')' ('->' | '=>') expression               # LambdaExpr
    | <assoc=right>  qualifiedId operator=':=' expression                    # WalrusExpr // TODO: Implement WalrusExpr
    |   expression (minus='-'? neg_precision=expression '.')? operator='~~' expression # ApproximatelyExpr
    |   expression operator='~~' ('.' minus='-'? precision=expression)? expression # ApproximatelyExpr
    |   acc=expression '.' variable=expression                  # VariableAccessExpr
    |   '(' type ')' expression                                 # CastExpr
    |   '(' expression ')'                                      # ParenthesizedExpr
    |   '[[' expressionList? ']]'                               # ArrayExpr // TODO: Implement ArrayExpr
    |   '[' expressionList? ']'                                 # ListExpr
    |   FIGURE_BRACKET_L expressionList? FIGURE_BRACKET_R       # SetExpr // TODO: Fix SetExpr
    |   '(' expressionList? ')'                                 # TupleExpr // TODO: Implement TupleExpr
    |   FIGURE_BRACKET_L expressionDict? FIGURE_BRACKET_R       # DictExpr // TODO: Implement DictExpr
    |   expression operator=('++' | '--')                       # PostfixExpr
    |   first=rangeToken? '..' second=rangeToken ('..' third=rangeToken)?          # RangeExpr
    |   '[' blk=block
    generator_for* ('if' filter=expression )? ']' # GeneratorExpr // TODO: Make generators lazy
    |   '[' item=expression
    generator_for* ('if' filter=expression )? ']' # GeneratorExpr

    |   ('!' expression | 'not' '(' expression ')')             # NotExpr
    |   operator=('++' | '--' | '+' | '-' | '~') expression     # UnaryExpr
    |   expression operator=('*' | '/' | '%' | 'mod') expression # MulDivExpr
    |   expression operator='/%' expression                     # FloorDivExpr
    |   expression operator='%/' expression                     # CeilDivExpr
    |   expression operator=('+' | '-') expression              # AddSubExpr
    |   expression operator=('<<' | '>>' | '>>>') expression    # ShiftExpr
    |   expression operator=('<' | '>' | '<=' | '>=') expression # RelationalExpr
    |   expression operator=('instanceof' | 'is') expression    # RelationalExpr
    |   expression operator=('==' | '!=') expression            # EqualityExpr
    |   expression '&' expression                               # BitwiseANDExpr
    |   expression '^' expression                               # BitwiseXORExpr
    |   expression '|' expression                               # BitwiseORExpr
    |   expression ('&&' | 'and') expression                    # LogicalANDExpr
    |   expression ('||' | 'or') expression                     # LogicalORExpr
    |   condition=expression '?' true_=expression ':' false_=expression # TernaryExpr
    |   true_=expression 'if' condition=expression 'else' false_=expression # TernaryExpr
    |   'new' type '(' expressionList? ')' block?               # CreationExpr
    |   ID '(' kwargs? ')'                                      # FunctionCallExpr

    // All of the expressions below must have an autoType in the
    // JavaCodeGeneratorVisitor.getAutoType(Object object) function
    |   qualifiedId                                             # IdExpr
    |   INT                                                     # IntExpr
    |   DECIMAL                                                 # DecimalExpr
    |   BOOL                                                    # BoolExpr
    |   fstring                                                 # FStringExpr
    |   STRING                                                  # StringExpr
    |   CHAR                                                    # CharExpr
    |   NULL                                                    # NullExpr
    ;

rangeToken : qualifiedId # RangeIdExpr | INT # RangeIntExpr ;

expressionList : expression (',' expression)* ;
expressionDict : dictElement (',' dictElement)* ;
dictElement : key=expression ':' value=expression ;

NULL : 'None' | 'null' ;

fragment STRING_SYMBOL : ( ESC_SEQ | ~[\\\r\n'{}] ) ;
fragment FSTRING_SYMBOL : ( ESC_SEQ | ~[\\\r\n'{}"] ) ;

CHAR : '\'' STRING_SYMBOL '\'' ;

STRING
    : '\'' STRING_SYMBOL*? '\''
    | '"'  STRING_SYMBOL*? '"'
    | 'f\'' STRING_SYMBOL*? '\''
    | 'f"'  STRING_SYMBOL*? '"'
    ;

FSTRING_EMPTY
    : '\'' FSTRING_SYMBOL*? '\''
    | '"'  FSTRING_SYMBOL*? '"'
    ;

kwargs
    : kwarg (',' kwarg)*
    | expression (',' expression)* (',' kwarg)*
    ;
kwarg : ID '=' expression ;

fragment ESC_SEQ
    : '\\' [btnfr"'\\]
    | '\\' 'u' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT
    ;

fragment HEX_DIGIT : [0-9a-fA-F] ;

INT :   [0-9]+ ('l' | 'L')? ;
DECIMAL : [0-9]+ ('.' [0-9]+)? ('f' | 'F' | 'd' | 'D')? ;
BOOL : ((('T' | 't') 'rue') | (('F' | 'f') 'alse')) ;
WILDCARD : '.*' ;

VOID : 'void' | 'def' ;
VAR : 'var' ;

ID  :   SYMBOL (SYMBOL | [0-9])* ;
idList : ID (',' ID)* ;
packedId : ID | idList ;
SYMBOL : (LETTER | '_' | '$') ;
TERMINATOR : ';' | ( '\r'? '\n' ' '* ) ;
qualifiedId : ID ('.' ID)*? ;

fragment LETTER : [a-zA-Z] ;
WS : (' ' | '\t') -> skip ;

SL_COMMENT : '//' ~[\r\n]* -> channel(HIDDEN) ;

ML_COMMENT : '/*' .*? '*/' -> channel(HIDDEN) ;