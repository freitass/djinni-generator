// AUTOGENERATED FILE - DO NOT MODIFY!
// This file generated by Djinni from my_record.djinni

#include <iostream> // for debugging
#include <cassert>
#include "wrapper_marshal.hpp"
#include "my_record.hpp"

#include "dh__map_string_int32_t.hpp"
#include "dh__my_record.hpp"
#include "dh__set_string.hpp"

static void(*s_py_callback_map_string_int32_t___delete)(DjinniObjectHandle *);
void map_string_int32_t_add_callback___delete(void(* ptr)(DjinniObjectHandle *)) {
    s_py_callback_map_string_int32_t___delete = ptr;
}

void map_string_int32_t___delete(DjinniObjectHandle * drh) {
    s_py_callback_map_string_int32_t___delete(drh);
}
void optional_map_string_int32_t___delete(DjinniOptionalObjectHandle *  drh) {
    s_py_callback_map_string_int32_t___delete((DjinniObjectHandle *) drh);
}
static int32_t ( * s_py_callback_map_string_int32_t__get_value)(DjinniObjectHandle *, DjinniString *);

void map_string_int32_t_add_callback__get_value(int32_t( * ptr)(DjinniObjectHandle *, DjinniString *)) {
    s_py_callback_map_string_int32_t__get_value = ptr;
}

static size_t ( * s_py_callback_map_string_int32_t__get_size)(DjinniObjectHandle *);

void map_string_int32_t_add_callback__get_size(size_t( * ptr)(DjinniObjectHandle *)) {
    s_py_callback_map_string_int32_t__get_size = ptr;
}

static DjinniObjectHandle * ( * s_py_callback_map_string_int32_t__python_create)(void);

void map_string_int32_t_add_callback__python_create(DjinniObjectHandle *( * ptr)(void)) {
    s_py_callback_map_string_int32_t__python_create = ptr;
}

static void ( * s_py_callback_map_string_int32_t__python_add)(DjinniObjectHandle *, DjinniString *, int32_t);

void map_string_int32_t_add_callback__python_add(void( * ptr)(DjinniObjectHandle *, DjinniString *, int32_t)) {
    s_py_callback_map_string_int32_t__python_add = ptr;
}

static DjinniString * ( * s_py_callback_map_string_int32_t__python_next)(DjinniObjectHandle *);

void map_string_int32_t_add_callback__python_next(DjinniString *( * ptr)(DjinniObjectHandle *)) {
    s_py_callback_map_string_int32_t__python_next = ptr;
}

djinni::Handle<DjinniObjectHandle> DjinniMapStringInt32T::fromCpp(const std::unordered_map<std::string, int32_t> & dc) {
    djinni::Handle<DjinniObjectHandle> _handle(s_py_callback_map_string_int32_t__python_create(), & map_string_int32_t___delete);
    for (const auto & it : dc) {
        auto _key = DjinniString::fromCpp(it.first);
        s_py_callback_map_string_int32_t__python_add(_handle.get(), _key.release(), it.second);
    }

    return _handle;
}

std::unordered_map<std::string, int32_t> DjinniMapStringInt32T::toCpp(djinni::Handle<DjinniObjectHandle> dh) {
    std::unordered_map<std::string, int32_t>_ret;
    size_t size = s_py_callback_map_string_int32_t__get_size(dh.get());

    for (int i = 0; i < size; i++) {
        auto _key_c = std::unique_ptr<DjinniString>(s_py_callback_map_string_int32_t__python_next(dh.get())); // key that would potentially be surrounded by unique pointer
        auto _val = s_py_callback_map_string_int32_t__get_value(dh.get(), _key_c.get());

        auto _key = DjinniString::toCpp(std::move(_key_c));
        _ret.emplace(std::move(_key), std::move(_val));
    }

    return _ret;
}

djinni::Handle<DjinniOptionalObjectHandle> DjinniMapStringInt32T::fromCpp(std::optional<std::unordered_map<std::string, int32_t>> dc) {
    if (dc == std::experimental::nullopt) {
        return nullptr;
    }
    return djinni::optionals::toOptionalHandle(DjinniMapStringInt32T::fromCpp(std::move(* dc)), optional_map_string_int32_t___delete);
}

std::optional<std::unordered_map<std::string, int32_t>>DjinniMapStringInt32T::toCpp(djinni::Handle<DjinniOptionalObjectHandle> dh) {
     if (dh) {
        return std::optional<std::unordered_map<std::string, int32_t>>(DjinniMapStringInt32T::toCpp(djinni::optionals::fromOptionalHandle(std::move(dh), map_string_int32_t___delete)));
    }
    return std::experimental::nullopt;
}

