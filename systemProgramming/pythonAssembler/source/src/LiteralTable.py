class literalInTable(Exception):
    def __str__(self):
        print("Literal이 List 내에 이미 존재합니다.")


class LiteralTable:
    literalList = None
    locationList = None

    def __init__(self):
        self.literalList = []
        self.locationList = []

    def putLiteral(self, literal, location):
        if self.search(literal) != -1:
            raise literalInTable
        else:
            self.literalList.append(literal)
            self.locationList.append(location)

    def search(self, literal):
        if literal in self.literalList:
            return self.locationList[self.literalList.index(literal)]
        else:
            return -1
