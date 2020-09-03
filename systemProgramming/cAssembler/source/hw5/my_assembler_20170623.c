/*
 * 화일명 : my_assembler_00000000.c 
 * 설  명 : 이 프로그램은 SIC/XE 머신을 위한 간단한 Assembler 프로그램의 메인루틴으로,
 * 입력된 파일의 코드 중, 명령어에 해당하는 OPCODE를 찾아 출력한다.
 * 파일 내에서 사용되는 문자열 "00000000"에는 자신의 학번을 기입한다.
 */

/*
 *
 * 프로그램의 헤더를 정의한다. 
 *
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <fcntl.h>

// 파일명의 "00000000"은 자신의 학번으로 변경할 것.
#include "my_assembler_20170623.h"

/* ----------------------------------------------------------------------------------
 * 설명 : 사용자로 부터 어셈블리 파일을 받아서 명령어의 OPCODE를 찾아 출력한다.
 * 매계 : 실행 파일, 어셈블리 파일 
 * 반환 : 성공 = 0, 실패 = < 0 
 * 주의 : 현재 어셈블리 프로그램의 리스트 파일을 생성하는 루틴은 만들지 않았다. 
 *		   또한 중간파일을 생성하지 않는다. 
 * ----------------------------------------------------------------------------------
 */
int main(int args, char *arg[])
{
	if (init_my_assembler() < 0)
	{
		printf("init_my_assembler: 프로그램 초기화에 실패 했습니다.\n");
		return -1;
	}

	if (assem_pass1() < 0)
	{
		printf("assem_pass1: 패스1 과정에서 실패하였습니다.  \n");
		return -1;
	}

	make_opcode_output("output_20170623");
	//make_opcode_output(NULL);


	/*
	* 추후 프로젝트에서 사용되는 부분
	
	make_symtab_output("symtab_00000000");
	if(assem_pass2() < 0 ){
		printf(" assem_pass2: 패스2 과정에서 실패하였습니다.  \n") ; 
		return -1 ; 
	}

	make_objectcode_output("output_00000000") ; 
	*/
	return 0;
}

/* ----------------------------------------------------------------------------------
 * 설명 : 프로그램 초기화를 위한 자료구조 생성 및 파일을 읽는 함수이다. 
 * 매계 : 없음
 * 반환 : 정상종료 = 0 , 에러 발생 = -1
 * 주의 : 각각의 명령어 테이블을 내부에 선언하지 않고 관리를 용이하게 하기 
 *		   위해서 파일 단위로 관리하여 프로그램 초기화를 통해 정보를 읽어 올 수 있도록
 *		   구현하였다. 
 * ----------------------------------------------------------------------------------
 */
int init_my_assembler(void)
{
	int result;

	if ((result = init_inst_file("inst.data")) < 0)
		return -1;
	if ((result = init_input_file("input.txt")) < 0)
		return -1;
	return result;
}

/* ----------------------------------------------------------------------------------
 * 설명 : 머신을 위한 기계 코드목록 파일을 읽어 기계어 목록 테이블(inst_table)을 
 *        생성하는 함수이다. 
 * 매계 : 기계어 목록 파일
 * 반환 : 정상종료 = 0 , 에러 < 0 
 * 주의 : 기계어 목록파일 형식은 자유롭게 구현한다. 예시는 다음과 같다.
 *	
 *	===============================================================================
 *		   | 이름 | 형식 | 기계어 코드 | 오퍼랜드의 갯수 | NULL|
 *	===============================================================================	   
 *		
 * ----------------------------------------------------------------------------------
 */
int init_inst_file(char *inst_file)
{
	//형식 : 이름  형식  코드  오퍼랜드개수
	FILE *file;
	int errno, line_count, count;
	const size_t line_size = 300;
	char line[300];
	char *tmp_word;
	/* add your code here */
	file = fopen(inst_file, "r");

	line_count = 0;
	while (fgets(line, line_size, file)) {
		inst_table[line_count] = (inst *)malloc(sizeof(inst));
		count = 0;
		tmp_word = strtok(line, "  ");
		while (tmp_word != NULL) {
			if (count == 0) {
				inst_table[line_count]->mnemonic = (char *)malloc(strlen(tmp_word) + 1);
				strcpy(inst_table[line_count]->mnemonic, tmp_word);
			}
			else if (count == 1) {
				inst_table[line_count]->format = (char *)malloc(strlen(tmp_word) + 1);
				strcpy(inst_table[line_count]->format, tmp_word);
			}
			else if (count == 2) {
				inst_table[line_count]->code = (char *)malloc(strlen(tmp_word) + 1);
				strcpy(inst_table[line_count]->code, tmp_word);
			}
			else if (count == 3) {
				inst_table[line_count]->n_operand = atoi(tmp_word);
			}
			count++;
			tmp_word = strtok(NULL, "  ");
		}
		line_count++;
	}
	inst_index = line_count;
	fclose(file);
	return errno;
}


/* ----------------------------------------------------------------------------------
 * 설명 : 어셈블리 할 소스코드를 읽어 소스코드 테이블(input_data)를 생성하는 함수이다. 
 * 매계 : 어셈블리할 소스파일명
 * 반환 : 정상종료 = 0 , 에러 < 0  
 * 주의 : 라인단위로 저장한다.
 *		
 * ----------------------------------------------------------------------------------
 */
int init_input_file(char *input_file)
{
	FILE *file;
	const size_t line_size = 300;
	char line[300];
	int errno, line_count;

	/* add your code here */
	file = fopen(input_file, "r");
	
	line_count = 0;
	while (fgets(line, line_size, file)) {
		input_data[line_count] = (char *)malloc(sizeof(line) + 1);
		strcpy(input_data[line_count], line);
		line_count++;
	}
	line_num = line_count;
	fclose(file);
	return errno;
}

/* ----------------------------------------------------------------------------------
 * 설명 : 소스 코드를 읽어와 토큰단위로 분석하고 토큰 테이블을 작성하는 함수이다. 
 *        패스 1로 부터 호출된다. 
 * 매계 : 파싱을 원하는 문자열  
 * 반환 : 정상종료 = 0 , 에러 < 0 
 * 주의 : my_assembler 프로그램에서는 라인단위로 토큰 및 오브젝트 관리를 하고 있다. 
 * ----------------------------------------------------------------------------------
 */
int token_parsing(char *str)
{
	/* add your code here */
	int change[10];
	int len = strlen(str);
	int i, j, k, x, index;
	char *tmp_word;
	char tmp_word2[4][100];

	for (i = 0; i < sizeof(token_table) / sizeof(token); i++)
		if (token_table[i] == NULL) break;
	index = i;

	j = 0;
	for (i = 0; i <= len; ) {
		if (str[i++] == '\t') {
			change[j++] = 1;
		}
		else {
			change[j++] = 0;
			while (str[i++] != '\t' && i < len);
			i--;
			if (i == len - 1) break;
		}
	}

	tmp_word = strtok(str, "\t");
	strcpy(tmp_word2[0], tmp_word);
	for (i = 1; ; i++) {
		tmp_word = strtok(NULL, "\t");
		if (tmp_word == NULL) break;
		strcpy(tmp_word2[i], tmp_word);
	}

	token_table[index] = (token *)calloc(MAX_LINES, sizeof(token));
	for (i = 0, j = 0, x = 0; i < sizeof(change) / sizeof(int); i++) {
		if (change[i] != 0 && change[i] != 1) break;
		j += change[i];
		if (change[i] == 0) {
				if (j == 0) {
					token_table[index]->label = (char *)malloc(strlen(tmp_word2[j]) + 1);
					strcpy(token_table[index]->label, tmp_word2[x]);
				}
				else if(j == 1) {
					token_table[index]->operator = (char *)malloc(strlen(tmp_word2[j]) + 1);
					strcpy(token_table[index]->operator, tmp_word2[x]);
				}
				else if (j == 2) {
					tmp_word = strtok(tmp_word2[x], ",");
					token_table[index]->operand[0] = (char *)malloc(strlen(tmp_word) + 1);
					strcpy(token_table[index]->operand[0], tmp_word);
					for (k = 1; ; k++) {
						tmp_word = strtok(NULL, ",");
						if (tmp_word == NULL) break;
						token_table[index]->operand[k] = (char *)malloc(strlen(tmp_word) + 1);
						strcpy(token_table[index]->operand[k], tmp_word);
					}
				}
				else if (j == 3) {
					token_table[index]->comment = (char *)malloc(strlen(tmp_word2[j]) + 1);
					strcpy(token_table[index]->comment, tmp_word2[x]);
				}
				else return -1;
				x++;
		}
	}

	return 0;
}

/* ----------------------------------------------------------------------------------
 * 설명 : 입력 문자열이 기계어 코드인지를 검사하는 함수이다. 
 * 매계 : 토큰 단위로 구분된 문자열 
 * 반환 : 정상종료 = 기계어 테이블 인덱스, 에러 < 0 
 * 주의 : 
 *		
 * ----------------------------------------------------------------------------------
 */
int search_opcode(char *str)
{
	/* add your code here */
	int i;
	char *only_code;

	if (str[0] == '+' || str[0] == '@') {
		only_code = (char *)malloc(strlen(str)); 
		for (i = 0; i <= strlen(str); i++) {
			only_code[i] = str[i + 1];
		}
	}
	else {
		only_code = (char *)malloc(strlen(str) + 1);
		strcpy(only_code, str);
	}
	for (i = 0; i < inst_index; i++) {
		if ((strcmp(only_code, inst_table[i]->mnemonic)) == 0) {
			return i;
		}
	}
	return -1;
}

/* ----------------------------------------------------------------------------------
* 설명 : 어셈블리 코드를 위한 패스1과정을 수행하는 함수이다.
*		   패스1에서는..
*		   1. 프로그램 소스를 스캔하여 해당하는 토큰단위로 분리하여 프로그램 라인별 토큰
*		   테이블을 생성한다.
*
* 매계 : 없음
* 반환 : 정상 종료 = 0 , 에러 = < 0
* 주의 : 현재 초기 버전에서는 에러에 대한 검사를 하지 않고 넘어간 상태이다.
*	  따라서 에러에 대한 검사 루틴을 추가해야 한다.
*
* -----------------------------------------------------------------------------------
*/
static int assem_pass1(void)
{
	/* add your code here */
	int i, result, x = 0;

	for (i = 0; i < line_num; i++) {
		if (input_data[i][0] == '.') {
			x++;
			continue;
		}
		if ((result = token_parsing(input_data[i])) == -1) break;
	}
	token_line = line_num - x;

	return result;
	/* input_data의 문자열을 한줄씩 입력 받아서 
	 * token_parsing()을 호출하여 token_unit에 저장
	 */
}

/* ----------------------------------------------------------------------------------
* 설명 : 입력된 문자열의 이름을 가진 파일에 프로그램의 결과를 저장하는 함수이다.
*        여기서 출력되는 내용은 명령어 옆에 OPCODE가 기록된 표(과제 4번) 이다.
* 매계 : 생성할 오브젝트 파일명
* 반환 : 없음
* 주의 : 만약 인자로 NULL값이 들어온다면 프로그램의 결과를 표준출력으로 보내어
*        화면에 출력해준다.
*        또한 과제 4번에서만 쓰이는 함수이므로 이후의 프로젝트에서는 사용되지 않는다.
* -----------------------------------------------------------------------------------
*/
void make_opcode_output(char *file_name)
{
	/* add your code here */
	FILE *file;
	char *lines;
	char *tab = "\t";
	int i, j;


	lines = (char *)calloc(MAX_LINES, sizeof(char) * 30);
	for (i = 0; i < token_line; i++) {
		if (token_table[i] == NULL) continue;
		if (token_table[i]->label != NULL)
			lines = strcat(lines, token_table[i]->label);
		lines = strcat(lines, tab);
		if (token_table[i]->operator != NULL)
			lines = strcat(lines, token_table[i]->operator);
		lines = strcat(lines, tab);
		for (j = 0; j < MAX_OPERAND && token_table[i]->operand[j] != NULL; j++) {
			lines = strcat(lines, token_table[i]->operand[j]);
			if (lines[strlen(lines) - 1] == '\n')
				lines[strlen(lines) - 1] = '\0';
			lines = strcat(lines, ",");
		}
		while (lines[strlen(lines) - 1] == ',') {
			lines[strlen(lines) - 1] = '\0';
		}
		if (search_opcode(token_table[i]->operator) != -1) {
			lines = strcat(lines, tab);
			lines = strcat(lines, inst_table[search_opcode(token_table[i]->operator)]->code);
		}
		lines = strcat(lines, "\n");
	}

	if (file_name == NULL) printf("%s", lines);
	else {
		file = fopen(file_name, "w");
		fprintf(file, lines);
		fclose(file);
	}
}

/* ----------------------------------------------------------------------------------
* 설명 : 입력된 문자열의 이름을 가진 파일에 프로그램의 결과를 저장하는 함수이다.
*        여기서 출력되는 내용은 SYMBOL별 주소값이 저장된 TABLE이다.
* 매계 : 생성할 오브젝트 파일명
* 반환 : 없음
* 주의 : 만약 인자로 NULL값이 들어온다면 프로그램의 결과를 표준출력으로 보내어
*        화면에 출력해준다.
*
* -----------------------------------------------------------------------------------
*/
void make_symtab_output(char *file_name)
{
	/* add your code here */
}

/* ----------------------------------------------------------------------------------
* 설명 : 입력된 문자열의 이름을 가진 파일에 프로그램의 결과를 저장하는 함수이다.
*        여기서 출력되는 내용은 LITERAL별 주소값이 저장된 TABLE이다.
* 매계 : 생성할 오브젝트 파일명
* 반환 : 없음
* 주의 : 만약 인자로 NULL값이 들어온다면 프로그램의 결과를 표준출력으로 보내어
*        화면에 출력해준다.
*
* -----------------------------------------------------------------------------------
*/
void make_literaltab_output(char *filen_ame)
{
	/* add your code here */
}

/* --------------------------------------------------------------------------------*
* ------------------------- 추후 프로젝트에서 사용할 함수 --------------------------*
* --------------------------------------------------------------------------------*/

/* ----------------------------------------------------------------------------------
* 설명 : 어셈블리 코드를 기계어 코드로 바꾸기 위한 패스2 과정을 수행하는 함수이다.
*		   패스 2에서는 프로그램을 기계어로 바꾸는 작업은 라인 단위로 수행된다.
*		   다음과 같은 작업이 수행되어 진다.
*		   1. 실제로 해당 어셈블리 명령어를 기계어로 바꾸는 작업을 수행한다.
* 매계 : 없음
* 반환 : 정상종료 = 0, 에러발생 = < 0
* 주의 :
* -----------------------------------------------------------------------------------
*/
static int assem_pass2(void)
{

	/* add your code here */
}

/* ----------------------------------------------------------------------------------
* 설명 : 입력된 문자열의 이름을 가진 파일에 프로그램의 결과를 저장하는 함수이다.
*        여기서 출력되는 내용은 object code (프로젝트 1번) 이다.
* 매계 : 생성할 오브젝트 파일명
* 반환 : 없음
* 주의 : 만약 인자로 NULL값이 들어온다면 프로그램의 결과를 표준출력으로 보내어
*        화면에 출력해준다.
*
* -----------------------------------------------------------------------------------
*/
void make_objectcode_output(char *file_name)
{
	/* add your code here */
}
