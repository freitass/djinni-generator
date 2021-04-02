# AUTOGENERATED FILE - DO NOT MODIFY!
# This file generated by Djinni from my_client_interface.djinni

import sys
from cffi import FFI
from djinni.support import *
ffi = FFI()

args = sys.argv[1:]
cdef_headers = sort_by_import_order(clean_headers_for(args, 'python_cdef_ignore'))
setsource_headers = sort_by_import_order(clean_headers_for(args, 'python_setsource_ignore'))

f = open('clean_headers.h', 'w')
f.write(setsource_headers)
f.close()

ffi.set_source('_cffi', '''#include "clean_headers.h"''',
    runtime_library_dirs=['.'])

ffi.cdef("typedef _Bool bool;"
 + cdef_headers)

if __name__ == '__main__':
    ffi.compile()

