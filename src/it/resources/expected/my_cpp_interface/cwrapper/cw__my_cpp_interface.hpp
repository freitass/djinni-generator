// AUTOGENERATED FILE - DO NOT MODIFY!
// This file generated by Djinni from my_cpp_interface.djinni

#pragma once

#include <atomic>
#include <optional>
#include "my_cpp_interface.hpp"
#ifdef __cplusplus
extern "C" {
#endif

#include "cw__my_cpp_interface.h"

#ifdef __cplusplus
}
#endif
struct DjinniWrapperMyCppInterface final {
    DjinniWrapperMyCppInterface(std::shared_ptr<::MyCppInterface>wo): wrapped_obj(wo) {};

    static std::shared_ptr<::MyCppInterface> get(djinni::Handle<DjinniWrapperMyCppInterface> dw);
    static djinni::Handle<DjinniWrapperMyCppInterface> wrap(std::shared_ptr<::MyCppInterface> obj);

    const std::shared_ptr<::MyCppInterface> wrapped_obj;
    std::atomic<size_t> ref_count {1};
};
