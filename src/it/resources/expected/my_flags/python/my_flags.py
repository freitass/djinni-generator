# AUTOGENERATED FILE - DO NOT MODIFY!
# This file generated by Djinni from my_flags.djinni

from djinni.support import MultiSet # default imported in all files
from djinni.exception import CPyException # default imported in all files

from djinni import exception # this forces run of __init__.py which gives cpp option to call back into py to create exception

from enum import IntEnum, unique

@unique
class MyFlags(IntEnum):
    Flag1 = 0
    Flag2 = 1
    Flag3 = 2
    No_Flags = 3
    All_Flags = 4
