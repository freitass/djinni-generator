// AUTOGENERATED FILE - DO NOT MODIFY!
// This file was generated by Djinni from all_datatypes.djinni

#import "ITAllDatatypes+Private.h"
#import "djinni/objc/DJIMarshal+Private.h"
#include <cassert>

namespace djinni_generated {

auto AllDatatypes::toCpp(ObjcType obj) -> CppType
{
    assert(obj);
    return {::djinni::Bool::toCpp(obj.booleanData),
            ::djinni::I8::toCpp(obj.integer8Data),
            ::djinni::I16::toCpp(obj.integer16Data),
            ::djinni::I32::toCpp(obj.integer32Data),
            ::djinni::I64::toCpp(obj.integer64Data),
            ::djinni::F32::toCpp(obj.float32Data),
            ::djinni::F64::toCpp(obj.float64Data),
            ::djinni::String::toCpp(obj.stringData),
            ::djinni::Binary::toCpp(obj.binaryData),
            ::djinni::Date::toCpp(obj.dateData),
            ::djinni::List<::djinni::Bool>::toCpp(obj.listData),
            ::djinni::Set<::djinni::Bool>::toCpp(obj.setData),
            ::djinni::Map<::djinni::I8, ::djinni::Bool>::toCpp(obj.mapData),
            ::djinni::Optional<std::optional, ::djinni::Bool>::toCpp(obj.optionalData)};
}

auto AllDatatypes::fromCpp(const CppType& cpp) -> ObjcType
{
    return [[ITAllDatatypes alloc] initWithBooleanData:(::djinni::Bool::fromCpp(cpp.booleanData))
                                          integer8Data:(::djinni::I8::fromCpp(cpp.integer8Data))
                                         integer16Data:(::djinni::I16::fromCpp(cpp.integer16Data))
                                         integer32Data:(::djinni::I32::fromCpp(cpp.integer32Data))
                                         integer64Data:(::djinni::I64::fromCpp(cpp.integer64Data))
                                           float32Data:(::djinni::F32::fromCpp(cpp.float32Data))
                                           float64Data:(::djinni::F64::fromCpp(cpp.float64Data))
                                            stringData:(::djinni::String::fromCpp(cpp.stringData))
                                            binaryData:(::djinni::Binary::fromCpp(cpp.binaryData))
                                              dateData:(::djinni::Date::fromCpp(cpp.dateData))
                                              listData:(::djinni::List<::djinni::Bool>::fromCpp(cpp.listData))
                                               setData:(::djinni::Set<::djinni::Bool>::fromCpp(cpp.setData))
                                               mapData:(::djinni::Map<::djinni::I8, ::djinni::Bool>::fromCpp(cpp.mapData))
                                          optionalData:(::djinni::Optional<std::optional, ::djinni::Bool>::fromCpp(cpp.optionalData))];
}

}  // namespace djinni_generated
