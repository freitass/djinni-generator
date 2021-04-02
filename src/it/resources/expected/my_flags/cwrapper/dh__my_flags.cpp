// AUTOGENERATED FILE - DO NOT MODIFY!
// This file generated by Djinni from my_flags.djinni

#include <iostream> // for debugging
#include <cassert>
#include "wrapper_marshal.hpp"
#include "my_flags.hpp"

int32_t int32_from_enum_my_flags(std::optional<::MyFlags> e) {
    if (e) {
        return static_cast<int32_t>(* e);
    }
    return -1; // -1 to signal null boxed enum
}

int32_t int32_from_enum_my_flags(::MyFlags e) {
    return static_cast<int32_t>(e);
}
std::optional<::MyFlags> get_boxed_enum_my_flags_from_int32(int32_t e) {
    if (e == -1) { // to signal null enum
        return std::experimental::nullopt;
    }
    return std::optional<::MyFlags>(static_cast<::MyFlags>(e));
}
