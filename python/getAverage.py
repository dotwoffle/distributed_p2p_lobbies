with open("runResults.csv", 'r') as resultFile:
    lines = resultFile.readlines()
    lines = [line.split(",") for line in lines]

avgs = []

for x in range(len(lines[0])):
    total = 0
    for y in range(len(lines)):
        total += int(lines[y][x])
    avgs.append(total/len(lines))

print(avgs)