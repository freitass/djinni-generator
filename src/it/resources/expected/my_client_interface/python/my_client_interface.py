# AUTOGENERATED FILE - DO NOT MODIFY!
# This file generated by Djinni from my_client_interface.djinni

from djinni.support import MultiSet # default imported in all files
from djinni.exception import CPyException # default imported in all files
from djinni.pycffi_marshal import CPyPrimitive, CPyString

from abc import ABCMeta, abstractmethod
from future.utils import with_metaclass
from _cffi import ffi, lib

from djinni import exception # this forces run of __init__.py which gives cpp option to call back into py to create exception

class MyClientInterface(with_metaclass(ABCMeta)):
    @abstractmethod
    def log_string(self, str):
        raise NotImplementedError


