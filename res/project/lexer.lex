# Lexer file
%DISCARD WHITESPACE: ['[ \t\r\n]']
%DISCARD COMMENT: %DELIMITER('/*', '', '*/')
                  ['//[^\r\n]*']

#COMPILER: ['#[a-zA-Z_][a-zA-Z0-9_]*']
IDENTIFIER: ['[a-zA-Z_][a-zA-Z0-9_]*']

DELIMITER:	
#'{' '}' '(' ')' '[' ']'
#'.' ';' ',' ':' '?'
			'+=' '-=' '*=' '/=' '%=' '^=' '>>=' '<<='
			'>>' '<<' '++' '--' '&=' '|='
#'+' '-' '*' '/' '%' '^' '&' '~'
			'||' '&&' '==' '>=' '<=' '!='
#'>' '<' '=' '|'

STRING: %DELIMITER('\"', '\\', '\"')
CHAR: %DELIMITER('\'', '\\', '\'')

FLOAT: ['[0-9]+[.][0-9]+[Ff]']
DOUBLE: ['[0-9]+[.][0-9]+[Dd]?']
LONG: ['0x[0-9a-fA-F]+L'] ['0b[0-1]+L'] ['[0-9]+L']
INT: ['0x[0-9a-fA-F]+'] ['0b[0-1]+'] ['[0-9]+']


TOKEN: ['.']

# BUILD_INCLUDE: 'include'
# BUILD_SPECIFY: 'specify'

# MODIFIER: 'export'

# Primitive types. (Always present with the compiler)
# PRIMITIVE: 'void' 'byte' 'char' 'bool' 'int' 'short' 'long' 'float' 'double'

# The most important keyword is 'asm'
# TODO: Have a group that only checks for groups aft
# KEYWORD: 'if' 'for' 'while' 'asm'
         # 'return' 'break' 'continue' 'as'

# This should be changed by the compiler
# TYPE_NAME: