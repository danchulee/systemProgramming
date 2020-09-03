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

	//make_opcode_output("output_20170623");
	//make_opcode_output(NULL);

	make_symtab_output("symtab_20170623");

	make_literaltab_output("literaltab_20170623");

	if (assem_pass2() < 0) {
		printf(" assem_pass2: 패스2 과정에서 실패하였습니다.  \n");
		return -1;
	}

	make_objectcode_output("output_20170623");
	//make_objectcode_output(NULL);

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
	input_file = inst_file;
	file = fopen(input_file, "r");

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
		if (line[0] == '.') continue;
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
	int i, j, k, x, index, search_index;
	int n, b, p, e;
	char *tmp_word;
	char tmp_word2[4][100];
	char buffer[5];

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

	str[strlen(str) - 1] = '\0';
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
			else if (j == 1) {
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
	free(tmp_word);

	search_index = search_opcode(token_table[index]->operator);
	if (search_index == -1) {
		token_table[index]->nixbpe = '\0';
	}
	else {
		//2형식
		if (strcmp(inst_table[search_index]->format, "2") == 0) {
			token_table[index]->nixbpe = '\0';
		}
		//3/4형식
		else {
			n = 1; i = 1; x = 0; b = 0; p = 1; e = 0;
			if (strchr(token_table[index]->operator,'+') != NULL) {
				p = 0;
				e = 1;
			}
			if (token_table[index]->operand[0] != NULL) {
				if (strchr(token_table[index]->operand[0], '#') != NULL) {
					n = 0; p = 0;
				}
				if (strchr(token_table[index]->operand[0], '@') != NULL) {
					i = 0;
				}
				if (token_table[index]->operand[1] != NULL) {
					if (token_table[index]->operand[1][0] == 'X')
						x = 1;
				}
			}
			else p = 0;
			tmp_word = (char *)calloc(8, sizeof(char));
			itoa(n, buffer, 10);  tmp_word = strcat(tmp_word, buffer);
			itoa(i, buffer, 10);  tmp_word = strcat(tmp_word, buffer);
			itoa(x, buffer, 10);  tmp_word = strcat(tmp_word, buffer);
			itoa(b, buffer, 10);  tmp_word = strcat(tmp_word, buffer);
			itoa(p, buffer, 10);  tmp_word = strcat(tmp_word, buffer);
			itoa(e, buffer, 10);  tmp_word = strcat(tmp_word, buffer);
			token_table[index]->nixbpe = (char)strtol(tmp_word, NULL, 2);
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
	int i, str_len;
	char *only_code;

	str_len = strlen(str);
	if (str[0] == '+' || str[0] == '@') {
		only_code = (char *)malloc(str_len);
		for (i = 0; i <= str_len; i++) {
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
	free(lines);
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
	//char 배열형 symbol이름 symbol, int형 주소 addr
	FILE *file;
	int i, j, index;
	char *lines, *tmp;
	char hex_addr[10];

	locctr = 0;

	lines = (char *)calloc(MAX_LINES, sizeof(char) * 30);
	for (i = 0; i < token_line; i++) {
		for (j = 0; j < sizeof(sym_table) / sizeof(symbol); j++)
			if (strcmp(sym_table[j].symbol, "") == 0) break;
		index = j;
		if (strcmp(token_table[i]->operator,"EXTDEF") == 0 || strcmp(token_table[i]->operator,"EXTREF") == 0) {
			locctr = 0;
			continue;
		}
		if (strcmp(token_table[i]->operator,"START") == 0 || strcmp(token_table[i]->operator,"CSECT") == 0) {
			locctr = 0;
			lines = strcat(lines, "\n");
		}

		if (token_table[i]->label != NULL) {
			if (strcmp(token_table[i]->operator,"EQU") == 0) {
				if (strcmp(token_table[i]->operand[0], "*") != 0) {
					strcpy(sym_table[index].symbol, token_table[i]->label);
					if (strstr(token_table[i]->operand[0], "+") != NULL
						|| strstr(token_table[i]->operand[0], "-") != NULL) {
						for (j = 0; strcmp(sym_table[j].symbol, "") != 0; j++) {
							if ((tmp = strstr(token_table[i]->operand[0], sym_table[j].symbol)) != NULL) {
								if ((tmp - 1)[0] == '-')
									sym_table[index].addr -= sym_table[j].addr;
								else
									sym_table[index].addr += sym_table[j].addr;
							}
						}
					}
					lines = strcat(lines, sym_table[index].symbol);
					lines = strcat(lines, "\t");
					sprintf(hex_addr, "%02X", sym_table[index].addr);
					lines = strcat(lines, hex_addr);
					lines = strcat(lines, "\n");
					continue;
				}
			}
			strcpy(sym_table[index].symbol, token_table[i]->label);
			sym_table[index].addr = locctr;
		}
		location_cnt(i);

		if (strcmp(sym_table[index].symbol, "") != 0) {
			lines = strcat(lines, sym_table[index].symbol);
			lines = strcat(lines, "\t");
			sprintf(hex_addr, "%02X", sym_table[index].addr);
			lines = strcat(lines, hex_addr);
			lines = strcat(lines, "\n");
		}
	}

	printf("*****SYMTAB*****\n%s\n", lines);
	if (file_name != NULL) {
		file = fopen(file_name, "w");
		fprintf(file, lines);
		fclose(file);
	}
	free(lines);

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
void make_literaltab_output(char *file_name)
{
	/* add your code here */
	FILE *file;
	int i, j, index, tmp_cnt, duplicate, saved;
	char *lines;
	char *tmp_lit[10];
	char hex_addr[10];
	locctr = 0; tmp_cnt = 0;

	for (j = 0; j < 10; j++)
		tmp_lit[j] = NULL;

	lines = (char *)calloc(MAX_LINES, sizeof(char) * 30);
	for (i = 0; i < token_line; i++) {
		duplicate = 0;

		for (j = 0; j < sizeof(literal_table) / sizeof(literal); j++)
			if (strcmp(literal_table[j].literal, "") == 0) break;
		index = j;

		if (strcmp(token_table[i]->operator,"EXTDEF") == 0 || strcmp(token_table[i]->operator,"EXTREF") == 0) {
			locctr = 0;
			continue;
		}
		if (strcmp(token_table[i]->operator,"START") == 0 || strcmp(token_table[i]->operator,"CSECT") == 0) {
			locctr = 0;
			saved = 0;
		}

		if (token_table[i]->operand[0] != NULL) {
			if (token_table[i]->operand[0][0] == '=') {
				for (j = 0; strcmp(literal_table[j].literal, "") != 0; j++) {
					if (strcmp(token_table[i]->operand[0], literal_table[j].literal) == 0) {
						duplicate = 1;
						break;
					}
				}
				for (j = 0; tmp_lit[j] != NULL; j++) {
					if (strcmp(token_table[i]->operand[0], tmp_lit[j]) == 0) {
						duplicate = 1;
						break;
					}
				}
				if (!duplicate) {
					tmp_lit[tmp_cnt] = (char *)calloc(strlen(token_table[i]->operand[0]) + 1, sizeof(char));
					strcpy(tmp_lit[tmp_cnt++], token_table[i]->operand[0]);
				}
			}
		}
		//LTORG 안 나오면 다 때려박는 거랑, 중복잇으면 뺌
		if (strcmp(token_table[i]->operator,"LTORG") == 0 || i == token_line - 1
			|| (strcmp(token_table[i + 1]->operator,"CSECT") == 0 && saved == 0)) {
			j = 0;
			while (j < tmp_cnt) {
				tmp_lit[j] = strtok(tmp_lit[j], "'");
				tmp_lit[j] = strtok(NULL, "'");
				strcpy(literal_table[index].literal, tmp_lit[j]);
				tmp_lit[j++] = NULL;
				literal_table[index].addr = locctr;
				lines = strcat(lines, literal_table[index].literal);
				lines = strcat(lines, "\t");
				sprintf(hex_addr, "%02X", literal_table[index].addr);
				lines = strcat(lines, hex_addr);
				lines = strcat(lines, "\n");
			}
			tmp_cnt = 0; saved = 1;
		}

		location_cnt(i);
	}

	printf("*****LITERALTAB*****\n%s\n", lines);
	if (file_name != NULL) {
		file = fopen(file_name, "w");
		fprintf(file, lines);
		fclose(file);
	}
	free(lines);
}

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
	//token table 받아서 input_data의 코드를 기계어로 변경하자
	//한 줄씩 돌면서... 주소 계산 하면서.. operand에 있는 거 symtab뒤져보고
	//있으면 symtab주소-(현재 주소+3) 해서 주소 계산
	//nixbpe에 search_opcode해서 받아온 코드 합치는 것도 잊지 말기)
	//ref도 검사.. 하면서 operand가 ref에 있으면 0으로채운다
	//1)operand symtab에서 발견? 
	//1-1)symtab에서 주소 끌고 와서 disp 계산
	//1-2)search_opcode로 operator 주소 끌고 옴
	//1-3)nixbpe랑 opcode랑 합핌
	//1-4)disp랑 합침
	char *ref[3];
	char line_code[20], disp[10], ex_disp[10];
	char *only_rand, *tmp_data, *tmp_lit = NULL;
	int tmp[10];
	int sym_start, i, j, k, refs = 0, done = 0, literal_yes = 0;
	locctr = 0;

	for (i = 0; i < token_line; i++) {
		free(input_data[i]); input_data[i] = NULL;
		strcpy(disp, ""); strcpy(ex_disp, ""); strcpy(line_code, "");
		done = 0;
		if (strcmp(token_table[i]->operator,"EXTDEF") == 0 || strcmp(token_table[i]->operator,"EXTREF") == 0) {
			locctr = 0;
			if (strcmp(token_table[i]->operator,"EXTREF") == 0) {
				refs = 1;
				for (j = 0; j < MAX_OPERAND; j++) {
					if (token_table[i]->operand[j] != NULL) {
						ref[j] = (char *)malloc(strlen(token_table[i]->operand[j]) + 1);
						strcpy(ref[j], token_table[i]->operand[j]);
					}
					else ref[j] = NULL;
				}
			}
			continue;
		}
		if (strcmp(token_table[i]->operator,"START") == 0 || strcmp(token_table[i]->operator,"CSECT") == 0) {
			for (j = 0; j < sizeof(sym_table) / sizeof(symbol); j++) {
				if (strcmp(token_table[i]->label, sym_table[j].symbol) == 0) {
					sym_start = j;
					break;
				}
			}
			locctr = 0;
			if (strcmp(token_table[i]->operator,"START") == 0) continue;
			else {
				refs = 0;
				literal_yes = 0;
				for (j = 0; j < MAX_OPERAND&&ref[j] != NULL; j++)
					free(ref[j]);
			}
		}
		if (strcmp(token_table[i]->operator,"LTORG") == 0 || i == token_line - 1) {
			if (literal_yes == 0) return -1;
			if (literal_table[literal_yes - 1].literal[0] >= 48 && literal_table[literal_yes - 1].literal[0] <= 57) {
				input_data[i] = (char *)malloc(strlen(literal_table[literal_yes - 1].literal) + 1);
				strcpy(input_data[i], literal_table[literal_yes - 1].literal);
			}
			else {
				k = strlen(literal_table[literal_yes - 1].literal);
				input_data[i] = (char *)malloc(k * 2 + 1);
				for (j = 0; j < k; j++) {
					tmp_data = (char *)malloc(3 * sizeof(char));
					tmp[j] = literal_table[literal_yes - 1].literal[j];
					itoa(tmp[j], tmp_data, 16);
					if (j == 0) strcpy(input_data[i], tmp_data);
					else input_data[i] = strcat(input_data[i], tmp_data);
					free(tmp_data);
				}
			}
			literal_yes = 0;
		}

		if (token_table[i]->operand[0] != NULL) {
			if (search_opcode(token_table[i]->operator) != -1) {
				if (strcmp(inst_table[search_opcode(token_table[i]->operator)]->format, "2") == 0) {
					input_data[i] = (char *)calloc(5, sizeof(char));
					strcpy(input_data[i], inst_table[search_opcode(token_table[i]->operator)]->code);
					for (j = 0; j < MAX_OPERAND&&token_table[i]->operand[j] != NULL; j++) {
						if (strcmp(token_table[i]->operand[j], "X") == 0)
							input_data[i] = strcat(input_data[i], "1");
						else if (strcmp(token_table[i]->operand[j], "A") == 0)
							input_data[i] = strcat(input_data[i], "0");
						else if (strcmp(token_table[i]->operand[j], "S") == 0)
							input_data[i] = strcat(input_data[i], "4");
						else if (strcmp(token_table[i]->operand[j], "T") == 0)
							input_data[i] = strcat(input_data[i], "5");
					}
					if (strlen(input_data[i]) == 3) input_data[i] = strcat(input_data[i], "0");
					locctr += 2;
					continue;
				}
			}
			else {
				if (strcmp(token_table[i]->operator,"BYTE") == 0) {
					tmp_data = (char *)calloc(strlen(token_table[i]->operand[0]) + 1, sizeof(char));
					strcpy(tmp_data, token_table[i]->operand[0]);
					tmp_data = strtok(tmp_data, "'");
					tmp_data = strtok(NULL, "'");
					input_data[i] = (char *)calloc(strlen(tmp_data) + 1, sizeof(char));
					strcpy(input_data[i], tmp_data);
					tmp_data = NULL;
				}
				if (strcmp(token_table[i]->operator,"WORD") == 0)
					for (j = 0; j < MAX_OPERAND&&ref[j] != NULL; j++)
						if (strstr(token_table[i]->operand[0], ref[j]) != NULL) {
							input_data[i] = (char *)malloc(strlen("000000") + 1);
							strcpy(input_data[i], "000000");
							break;
						}
			}

			if (token_table[i]->operand[0][0] == '@') {
				only_rand = (char *)malloc(strlen(token_table[i]->operand[0]));
				k = strlen(token_table[i]->operand[0]);
				for (j = 0; j <= k; j++) {
					only_rand[j] = token_table[i]->operand[0][j + 1];
				}
			}
			else {
				only_rand = (char *)malloc(strlen(token_table[i]->operand[0]) + 1);
				strcpy(only_rand, token_table[i]->operand[0]);
			}

			if (only_rand[0] == '#') {
				strcpy(disp, "00");
				size_t sz = strlen(disp);
				disp[sz] = only_rand[1];
				disp[sz + 1] = '\0';
			}
			else if (only_rand[0] == '=') {
				literal_yes++;
				tmp_lit = (char *)calloc(strlen(token_table[i]->operand[0]) + 1, sizeof(char));
				tmp_lit = strtok(token_table[i]->operand[0], "'");
				tmp_lit = strtok(NULL, "'");
				for (j = 0; j < MAX_LINES; j++)
					if (strcmp(tmp_lit, literal_table[j].literal) == 0) {
						itoa(literal_table[j].addr - (locctr + 3), disp, 16);
						break;
					}
				tmp_lit = NULL;
			}
			else {
				if (refs)
					for (j = 0; j < MAX_OPERAND&&ref[j] != NULL; j++)
						if (strcmp(only_rand, ref[j]) == 0) {
							if (token_table[i]->operator[0] != '+') return -1;
							strcpy(disp, "00000");
							done = 1;
							break;
						}
				if (!done)
					for (j = sym_start; j < MAX_LINES; j++)
						if (strcmp(only_rand, sym_table[j].symbol) == 0) {
							itoa(sym_table[j].addr - (locctr + 3), disp, 16);
							if (disp[0] == 'f') {
								k = strlen(disp);
								k -= 3;
								for (j = 0; j < k; j++)
									disp[j] = disp[j + k];
								disp[j] = '\0';
							}
							break;
						}
			}
		}
		else {
			if (search_opcode(token_table[i]->operator) != -1)
				strcpy(disp, "000");
		}

		location_cnt(i);

		if (strcmp(disp, "") != 0 && token_table[i]->nixbpe != '\0') {
			strcpy(ex_disp, opcode_plus_field(inst_table[search_opcode(token_table[i]->operator)]->code, token_table[i]->nixbpe));
			strcat(line_code, ex_disp);
			if (strlen(disp) == 2) strcat(line_code, "0");
			else if (strlen(disp) == 1) strcat(line_code, "00");
			else if (strlen(disp) > 5) return -1;
			strcat(line_code, disp);
			input_data[i] = (char *)malloc(strlen(line_code) + 1);
			strcpy(input_data[i], line_code);
		}
		if (input_data[i] != NULL) {
			k = strlen(input_data[i]);
			for (j = 0; j < k; j++) {
				if (input_data[i][j] >= 97 && input_data[i][j] <= 122)
					input_data[i][j] = input_data[i][j] - 32;
			}
		}
	}


	return 0;
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
	//hex string -> 숫자 -> hex string
	FILE *file;
	char *sect, *tmp_len, *ref_ptr, *refs[20];
	char hex_tmp[20], *m_loct[20], *m_len[20], *m_ref[20];
	int letter_cnt = 0, section = 0;
	int i, j, k, m, n, p, last_loct = 0;

	for (i = 0; i < 20; i++) {
		m_loct[i] = NULL; m_len[i] = NULL; m_ref[i] = NULL;
		refs[i] = NULL;
	}

	sect = (char *)calloc(200, sizeof(char) * 70);
	for (i = 0; i < line_num; i++) {
		if (strcmp(token_table[i]->operator,"START") == 0 || strcmp(token_table[i]->operator,"CSECT") == 0) {
			if (strcmp(token_table[i]->operator,"CSECT") == 0) {
				for (j = 0; refs[j] != NULL; j++) {
					free(refs[j]);
					refs[j] = NULL;
				}
				tmp_len = strstr(sect, "XX");
				sprintf(hex_tmp, "%02X", letter_cnt / 2);
				for (j = 0; j < 2; j++)
					tmp_len[j] = hex_tmp[j];
				section++;
				sect = strcat(sect, "\n");
				tmp_len = strstr(sect, "AAAAAA");
				sprintf(hex_tmp, "%06X", locctr);
				if (strcmp(token_table[i - 1]->operator,"EQU") == 0)
					if (strcmp(token_table[i - 1]->operand[0], "*") != 0)
						sprintf(hex_tmp, "%06X", locctr - 3);

				for (j = 0; j < 6; j++)
					tmp_len[j] = hex_tmp[j];
				for (j = 0; m_loct[j] != NULL; j++) {
					sect = strcat(sect, "M");
					sect = strcat(sect, m_loct[j]);
					sect = strcat(sect, m_len[j]);
					sect = strcat(sect, m_ref[j]);
					sect = strcat(sect, "\n");
					free(m_loct[j]); m_loct[j] = NULL;
					free(m_len[j]); m_len[j] = NULL;
					free(m_ref[j]); m_ref[j] = NULL;
				}

				sect = strcat(sect, "E");
				if (section == 1) {
					sect = strcat(sect, "000000");
				}
				sect = strcat(sect, "\n\n");
			}
			letter_cnt = 0; locctr = 0;
			sect = strcat(sect, "H");
			sect = strcat(sect, token_table[i]->label);
			sect = strcat(sect, " ");
			sprintf(hex_tmp, "%06X", locctr);
			sect = strcat(sect, hex_tmp);

			sect = strcat(sect, "AAAAAA");
			sect = strcat(sect, "\n");
			continue;
		}

		if (strcmp(token_table[i]->operator,"EXTDEF") == 0) {
			sect = strcat(sect, "D");
			for (j = 0; j < MAX_OPERAND&&token_table[i]->operand[j] != NULL; j++) {
				sect = strcat(sect, token_table[i]->operand[j]);
				for (k = 0; k < MAX_LINES&&strcmp(sym_table[k].symbol, "") != 0; k++) {
					if (strcmp(token_table[i]->operand[j], sym_table[k].symbol) == 0) {
						sprintf(hex_tmp, "%06X", sym_table[k].addr);
						sect = strcat(sect, hex_tmp);
						break;
					}
				}
			}
			sect = strcat(sect, "\n");
			continue;
		}
		if (strcmp(token_table[i]->operator,"EXTREF") == 0) {
			sect = strcat(sect, "R");
			for (j = 0; j < MAX_OPERAND&&token_table[i]->operand[j] != NULL; j++) {
				sect = strcat(sect, token_table[i]->operand[j]);
				refs[j] = (char *)malloc(strlen(token_table[i]->operand[j]) + 1);
				strcpy(refs[j], token_table[i]->operand[j]);
			}
			sect = strcat(sect, "\n");
			continue;
		}

		if (strcmp(token_table[i]->operator, "EQU") == 0)
			if (strcmp(token_table[i]->operand[0], "*") != 0)
				continue;


		for (j = 0; j < MAX_OPERAND&&refs[j] != NULL && token_table[i]->operand[0] != NULL; j++) {
			m = 0; p = 0;
			ref_ptr = strstr(token_table[i]->operand[0], refs[j]);
			if (ref_ptr != NULL) {
				for (k = 0; k < sizeof(m_loct); k++)
					if (m_loct[k] == NULL) break;
				for (n = 0; n < (int)strlen(input_data[i]); n += 2) {
					if (input_data[i][n] != 0 && input_data[i][n + 1] != '0') p++;
					else break;
				}
				sprintf(hex_tmp, "%06X", locctr + p);
				m_loct[k] = (char *)malloc(strlen(hex_tmp) + 1);
				strcpy(m_loct[k], hex_tmp);

				for (n = 0; n < (int)strlen(input_data[i]); n++)
					if (input_data[i][n] == '0') m++;
				sprintf(hex_tmp, "%02X", m);
				m_len[k] = (char *)malloc(strlen(hex_tmp) + 2);
				strcpy(m_len[k], hex_tmp);
				if ((ref_ptr - 1)[0] == '-') {
					m_len[k][strlen(hex_tmp)] = '-';
				}
				else {
					m_len[k][strlen(hex_tmp)] = '+';
				}
				m_len[k][strlen(hex_tmp) + 1] = '\0';

				m_ref[k] = (char *)malloc(strlen(refs[j]) + 1);
				strcpy(m_ref[k], refs[j]);


			}
		}

		if (input_data[i] != NULL) {
			if (locctr - last_loct > 4) {
				tmp_len = strstr(sect, "XX");
				sprintf(hex_tmp, "%02X", letter_cnt / 2);
				for (j = 0; j < 2; j++)
					tmp_len[j] = hex_tmp[j];
				letter_cnt = 0;
				sect = strcat(sect, "\n");
			}
			last_loct = locctr;
			if (letter_cnt == 0) {
				sect = strcat(sect, "T");
				sprintf(hex_tmp, "%06X", locctr);
				sect = strcat(sect, hex_tmp);
				sect = strcat(sect, "XX");
			}
			if (letter_cnt + strlen(input_data[i]) <= 60) {
				letter_cnt += strlen(input_data[i]);
				sect = strcat(sect, input_data[i]);

			}
			else {
				tmp_len = strstr(sect, "XX");
				sprintf(hex_tmp, "%02X", letter_cnt / 2);
				for (j = 0; j < 2; j++)
					tmp_len[j] = hex_tmp[j];
				sect = strcat(sect, "\n");
				letter_cnt = 0;
				i--;
				continue;
			}
		}

		if (i != line_num - 1) location_cnt(i);
		else {
			locctr += (strlen(input_data[i]) / 2);
			tmp_len = strstr(sect, "XX");
			sprintf(hex_tmp, "%02X", letter_cnt / 2);
			for (j = 0; j < 2; j++)
				tmp_len[j] = hex_tmp[j];

			tmp_len = strstr(sect, "AAAAAA");
			sprintf(hex_tmp, "%06X", locctr);
			for (j = 0; j < 6; j++)
				tmp_len[j] = hex_tmp[j];
			sect = strcat(sect, "\n");
			for (j = 0; m_loct[j] != NULL; j++) {
				sect = strcat(sect, "M");
				sect = strcat(sect, m_loct[j]);
				sect = strcat(sect, m_len[j]);
				sect = strcat(sect, m_ref[j]);
				sect = strcat(sect, "\n");
				free(m_loct[j]); m_loct[j] = NULL;
				free(m_len[j]); m_len[j] = NULL;
				free(m_ref[j]); m_ref[j] = NULL;
			}
			sect = strcat(sect, "E");
		}
	}
	output_file = file_name;

	if (output_file == NULL) printf("%s", sect);
	else {
		file = fopen(output_file, "w");
		fprintf(file, sect);
		fclose(file);
	}
}


char *opcode_plus_field(char *opcode, char nixbpe)
{
	int i, op, j = 0;
	char *code_ex_disp, *front, *back; //opcode 뒤 field + ni, xbpe field
	char tmp[10], tmp1[10], tmp2[10];

	//xbpe만 합쳐서 hex 하나로
	code_ex_disp = (char *)malloc(3 * sizeof(char));
	front = (char *)malloc(5 * sizeof(char));
	back = (char *)malloc(5 * sizeof(char));

	code_ex_disp[0] = opcode[0];
	code_ex_disp[1] = '\0';
	tmp1[0] = opcode[1]; //앞에서 두번째 string 형 
	tmp1[1] = '\0';
	op = (int)strtol(tmp1, NULL, 16); //숫자로 변환

	strcpy(tmp, dec_to_binstr(nixbpe));
	for (i = 0; i < 6; i++) { //nixbpe 돌음
		if (i < 2) { //n,i 는 op채움
			if (i == 0)
				if (tmp[i] == '1') op += 2;
			if (i == 1)
				if (tmp[i] == '1') op += 1;
		}//아니면 일단 string으로
		else back[j++] = tmp[i];
	}
	back[j] = '\0';
	//back은 현재 binary 형태 string.. string->int->hex string 변환시켜야 함
	//code_ex_disp + front + back
	sprintf(front, "%01X", op);
	code_ex_disp = strcat(code_ex_disp, front);
	//tmp2[0] = strtol(back, NULL, 2) + '0';
	i = strtol(back, NULL, 2);
	sprintf(tmp2, "%01X", i);
	code_ex_disp = strcat(code_ex_disp, tmp2);
	free(front);
	free(back);

	return code_ex_disp;
}

char *dec_to_binstr(int dec)
{
	int i, j = 0;
	char bin_tmp[10];
	char *bin;

	for (i = 0; dec >= 1; i++) {
		bin_tmp[i] = (dec % 2) + '0';
		dec /= 2;
	}
	bin_tmp[i] = '\0';
	if (strlen(bin_tmp) == 5) {
		size_t sz = strlen(bin_tmp);
		bin_tmp[sz++] = '0';
		bin_tmp[sz] = '\0';
	}
	bin = (char *)malloc(strlen(bin_tmp) + 1);
	for (i = strlen(bin_tmp) - 1; i >= 0; i--) {
		bin[j++] = bin_tmp[i];
	}
	bin[j] = '\0';

	return bin;
}

void location_cnt(int index)
{
	if (strcmp(token_table[index]->operator,"RESB") == 0)
		locctr += atoi(token_table[index]->operand[0]);
	else if (strcmp(token_table[index]->operator,"RESW") == 0)
		locctr += (atoi(token_table[index]->operand[0]) * 3);
	else if (strcmp(token_table[index]->operator,"BYTE") == 0)
		locctr += 1;
	else locctr += 3;

	if (search_opcode(token_table[index]->operator) != -1) {
		if (strcmp(inst_table[search_opcode(token_table[index]->operator)]->format, "2") == 0) locctr--;
		else if (token_table[index]->operator[0] == '+') locctr++;
	}
}
