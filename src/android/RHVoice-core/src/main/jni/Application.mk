APP_STL := c++_static
# Work around clang enum-constexpr-conversion diagnostics triggered by Boost MPL with NDK r27+
APP_CPPFLAGS += -Wno-enum-constexpr-conversion
