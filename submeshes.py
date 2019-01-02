import json
import os
import sys, getopt

def createSubmeshesJSON(argv):
    filepath = ''
    try:
        opts, args = getopt.getopt(argv, "hi:", ["input="])
    except getopt.GetoptError:
        print('submeshes.py -i <inputfile>')
        sys.exit(2)
    for opt, arg in opts:
        if opt == '-h':
            print('submeshes.py -i <inputfile>')
            sys.exit()
        elif opt in ("-i", "--input"):
            filepath = arg
    try:
        infile = open(filepath, "r")
    except FileNotFoundError:
        print('file is not found: set -i, --input argument or check filepath')
        sys.exit(2)

    root, ext = os.path.splitext(filepath)
    data = {}
    data['submeshes'] = []
    line = infile.readline()
    index = 0

    while line:
        if 'usemtl' in line:
            marker, value = line.split()
            if not any(d['name'] == value for d in data['submeshes']):
                data['submeshes'].append({
                    'index': index,
                    'name': value
                })
                index += 1

        line = infile.readline()

    infile.close()

    if data['submeshes']:
        with open(root+'_meshes.json', "w") as outfile:
            json.dump(data, outfile)
        print("Submeshes data file created: ", os.path.realpath(outfile.name))
    else:
        print("There is no submeshes data")
createSubmeshesJSON(sys.argv[1:])
