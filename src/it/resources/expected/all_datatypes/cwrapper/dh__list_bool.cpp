// AUTOGENERATED FILE - DO NOT MODIFY!
// This file generated by Djinni from all_datatypes.djinni

#include <iostream> // for debugging
#include <cassert>
#include "wrapper_marshal.hpp"
#include "all_datatypes.hpp"

#include "dh__all_datatypes.hpp"
#include "dh__list_bool.hpp"
#include "dh__map_int8_t_bool.hpp"
#include "dh__set_bool.hpp"
#include <chrono>
#include <optional>
#include <vector>

static void(*s_py_callback_list_bool___delete)(DjinniObjectHandle *);
void list_bool_add_callback___delete(void(* ptr)(DjinniObjectHandle *)) {
    s_py_callback_list_bool___delete = ptr;
}

void list_bool___delete(DjinniObjectHandle * drh) {
    s_py_callback_list_bool___delete(drh);
}
void optional_list_bool___delete(DjinniOptionalObjectHandle *  drh) {
    s_py_callback_list_bool___delete((DjinniObjectHandle *) drh);
}
static bool ( * s_py_callback_list_bool__get_elem)(DjinniObjectHandle *, size_t);

void list_bool_add_callback__get_elem(bool( * ptr)(DjinniObjectHandle *, size_t)) {
    s_py_callback_list_bool__get_elem = ptr;
}

static size_t ( * s_py_callback_list_bool__get_size)(DjinniObjectHandle *);

void list_bool_add_callback__get_size(size_t( * ptr)(DjinniObjectHandle *)) {
    s_py_callback_list_bool__get_size = ptr;
}

static DjinniObjectHandle * ( * s_py_callback_list_bool__python_create)(void);

void list_bool_add_callback__python_create(DjinniObjectHandle *( * ptr)(void)) {
    s_py_callback_list_bool__python_create = ptr;
}

static void ( * s_py_callback_list_bool__python_add)(DjinniObjectHandle *, bool);

void list_bool_add_callback__python_add(void( * ptr)(DjinniObjectHandle *, bool)) {
    s_py_callback_list_bool__python_add = ptr;
}

djinni::Handle<DjinniObjectHandle> DjinniListBool::fromCpp(const std::vector<bool> & dc) {
    djinni::Handle<DjinniObjectHandle> _handle(s_py_callback_list_bool__python_create(), & list_bool___delete);
    size_t size = dc.size();
    for (int i = 0; i < size; i++) {
        s_py_callback_list_bool__python_add(_handle.get(), dc[i]);
    }

    return _handle;
}

std::vector<bool> DjinniListBool::toCpp(djinni::Handle<DjinniObjectHandle> dh) {
    std::vector<bool>_ret;
    size_t size = s_py_callback_list_bool__get_size(dh.get());
    _ret.reserve(size);

    for (int i = 0; i < size; i++) {
        _ret.push_back(s_py_callback_list_bool__get_elem(dh.get(), i));
    }

    return _ret;
}

djinni::Handle<DjinniOptionalObjectHandle> DjinniListBool::fromCpp(std::optional<std::vector<bool>> dc) {
    if (dc == std::experimental::nullopt) {
        return nullptr;
    }
    return djinni::optionals::toOptionalHandle(DjinniListBool::fromCpp(std::move(* dc)), optional_list_bool___delete);
}

std::optional<std::vector<bool>>DjinniListBool::toCpp(djinni::Handle<DjinniOptionalObjectHandle> dh) {
     if (dh) {
        return std::optional<std::vector<bool>>(DjinniListBool::toCpp(djinni::optionals::fromOptionalHandle(std::move(dh), list_bool___delete)));
    }
    return std::experimental::nullopt;
}

