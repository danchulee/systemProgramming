import sys


class parsingError(Exception):
    def __str__(self):
        print("Parsing Error 발생")
        sys.exit()


class InstTable:
    instMap = {}

    def __init__(self):
        self.instMap = {}

    def __init__(self, instFile):
        self.instMap = {}
        self.openFile(instFile)

    def openFile(self, fileName):
        try:
            file = open(fileName, 'r')
            while True:
                line = file.readline()
                if not line: break
                inst = Instruction(line)
                self.instMap[inst.instruction] = inst
        except IOError as e:
            print(e)
            sys.exit()
        finally: file.close()

    def inst_info(self, instruction):
        if instruction[0] == '+':
            instruction = instruction[1:]
        if self.instMap.get(instruction) is None:
            return None
        return self.instMap[instruction]


class Instruction:

    def __init__(self, line):
        self.format = None
        self.instruction = None
        self.opcode = None
        self.n_operand = None
        self.parsing(line)

    def parsing(self, line):
        parts = line.split()
        for (i, part) in enumerate(parts):
            if i == 0:
                self.instruction = part
            elif i == 1:
                self.format = part
            elif i == 2:
                self.opcode = part
            elif i == 3:
                self.n_operand = int(part)
            else:
                raise parsingError
