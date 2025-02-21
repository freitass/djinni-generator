/** Copyright 2014 Dropbox, Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy
  * of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations
  * under the License.
  */

package djinni

import java.io.StringWriter

import djinni.ast._
import djinni.generatorTools._
import djinni.meta._
import djinni.writer.IndentWriter

import scala.collection.mutable

class ObjcppGenerator(spec: Spec) extends BaseObjcGenerator(spec) {
  val objcMarshal = new ObjcMarshal(spec)
  val objcppMarshal = new ObjcppMarshal(spec)
  val cppMarshal = new CppMarshal(spec)

  class ObjcRefs() {
    var body = mutable.TreeSet[String]()
    var privHeader = mutable.TreeSet[String]()

    def find(ty: TypeRef) { find(ty.resolved) }
    def find(tm: MExpr) {
      tm.args.foreach(find)
      find(tm.base)
    }
    def find(m: Meta) = for (r <- objcppMarshal.references(m)) r match {
      case ImportRef(arg) => body.add("#import " + arg)
      case _              =>
    }
  }

  private def arcAssert(w: IndentWriter) = w.wl(
    "static_assert(__has_feature(objc_arc), " + q(
      "Djinni requires ARC to be enabled for this file"
    ) + ");"
  )

  override def generateEnum(origin: String, ident: Ident, doc: Doc, e: Enum) {
    var imports = mutable.TreeSet[String]()
    imports.add(
      "#import " + q(
        objcppMarshal.objcBaseLibIncludePrefix + "DJIMarshal+Private.h"
      )
    )
    imports.add(
      "!#include " + q(
        spec.objcppIncludeCppPrefix + spec.cppFileIdentStyle(
          ident
        ) + "." + spec.cppHeaderExt
      )
    )

    writeObjcFile(ident.name, isHeader = true, origin, imports, w => {})
  }

  def headerName(ident: String): String =
    idObjc.ty(ident) + "." + spec.objcHeaderExt
  def privateBodyName(ident: String): String =
    idObjc.ty(ident) + "+Private." + spec.objcppExt

  def nnCheck(expr: String): String =
    spec.cppNnCheckExpression.fold(expr)(check => s"$check($expr)")

  override def generateInterface(
      origin: String,
      ident: Ident,
      doc: Doc,
      typeParams: Seq[TypeParam],
      i: Interface
  ) {
    val refs = new ObjcRefs()
    i.methods.map(m => {
      m.params.map(p => refs.find(p.ty))
      m.ret.foreach(refs.find)
    })
    i.consts.map(c => {
      refs.find(c.ty)
    })

    val self = objcMarshal.typename(ident, i)
    val cppSelf = cppMarshal.fqTypename(ident, i)

    refs.privHeader.add("#include <memory>")
    refs.privHeader.add(
      "!#include " + q(
        spec.objcppIncludeCppPrefix + spec.cppFileIdentStyle(
          ident
        ) + "." + spec.cppHeaderExt
      )
    )
    refs.body.add(
      "!#import " + q(spec.objcppIncludeObjcPrefix + headerName(ident))
    )
    refs.body.add(
      "!#import " + q(
        spec.objcppIncludePrefix + objcppMarshal.privateHeaderName(ident.name)
      )
    )

    spec.cppNnHeader match {
      case Some(nnHdr) => refs.privHeader.add("#include " + nnHdr)
      case _           =>
    }

    def writeObjcFuncDecl(method: Interface.Method, w: IndentWriter) {
      val label = if (method.static) "+" else "-"
      val ret = objcMarshal.fqReturnType(method.ret)
      val decl = s"$label ($ret)${idObjc.method(method.ident)}"
      writeAlignedObjcCall(
        w,
        decl,
        method.params,
        "",
        p =>
          (
            idObjc.field(p.ident),
            s"(${objcMarshal.paramType(p.ty)})${idObjc.local(p.ident)}"
          )
      )
    }

    val helperClass = objcppMarshal.helperClass(ident)

    writeObjcFile(
      ident.name,
      isHeader = true,
      origin,
      refs.privHeader,
      w => {
        arcAssert(w)
        w.wl
        w.wl((if (i.ext.objc) "@protocol " else "@class ") + self + ";")
        w.wl
        wrapNamespace(
          w,
          spec.objcppNamespace,
          w => {
            w.wl(s"class $helperClass").bracedSemi {
              w.wlOutdent("public:")
              spec.cppNnType match {
                case Some(nnPtr) =>
                  w.wl(s"using CppType = $nnPtr<$cppSelf>;")
                  w.wl(s"using CppOptType = std::shared_ptr<$cppSelf>;")
                case _ =>
                  w.wl(s"using CppType = std::shared_ptr<$cppSelf>;")
                  w.wl(s"using CppOptType = std::shared_ptr<$cppSelf>;")
              }
              w.wl(
                "using ObjcType = " + (if (i.ext.objc) s"id<$self>"
                                       else s"::$self*") + ";"
              )
              w.wl
              w.wl(s"using Boxed = $helperClass;")
              w.wl
              w.wl(s"static CppType toCpp(ObjcType objc);")
              w.wl(s"static ObjcType fromCppOpt(const CppOptType& cpp);")
              w.wl(
                s"static ObjcType fromCpp(const CppType& cpp) { return fromCppOpt(cpp); }"
              )
              w.wl
              w.wlOutdent("private:")
              w.wl("class ObjcProxy;")
            }
          }
        )
        w.wl
      }
    )

    if (i.ext.cpp) {
      refs.body.add(
        "#import " + q(
          objcppMarshal.objcBaseLibIncludePrefix + "DJICppWrapperCache+Private.h"
        )
      )
      refs.body.add("#include <utility>")
      refs.body.add(
        "#import " + q(objcppMarshal.objcBaseLibIncludePrefix + "DJIError.h")
      )
      refs.body.add("#include <exception>")
    }
    if (spec.cppNnType.isDefined || spec.cppNnCheckExpression.isEmpty) {
      refs.body.add("#include <stdexcept>")
    }

    if (i.ext.objc) {
      refs.body.add(
        "#import " + q(
          objcppMarshal.objcBaseLibIncludePrefix + "DJIObjcWrapperCache+Private.h"
        )
      )
    }

    if (!i.ext.cpp && !i.ext.objc) {
      refs.body.add(
        "#import " + q(objcppMarshal.objcBaseLibIncludePrefix + "DJIError.h")
      )
    }

    writeObjcFile(
      ident.name,
      isHeader = false,
      origin,
      refs.body,
      w => {
        arcAssert(w)

        val objcSelf = if (i.ext.objc && i.ext.cpp) self + "CppProxy" else self

        if (i.ext.cpp) {
          w.wl
          if (i.ext.objc)
            w.wl(s"@interface $objcSelf : NSObject<$self>")
          else
            w.wl(s"@interface $objcSelf ()")
          w.wl
          w.wl(s"- (id)initWithCpp:(const std::shared_ptr<$cppSelf>&)cppRef;")
          w.wl
          w.wl("@end")
          w.wl
          w.wl(s"@implementation $objcSelf {")
          w.wl(
            s"    ::djinni::CppProxyCache::Handle<std::shared_ptr<$cppSelf>> _cppRefHandle;"
          )
          w.wl("}")
          w.wl
          w.wl(s"- (id)initWithCpp:(const std::shared_ptr<$cppSelf>&)cppRef")
          w.braced {
            w.w("if (self = [super init])").braced {
              w.wl("_cppRefHandle.assign(cppRef);")
            }
            w.wl("return self;")
          }
          for (m <- i.methods) {
            w.wl
            writeObjcFuncDecl(m, w)
            w.braced {
              w.w("try").bracedEnd(" DJINNI_TRANSLATE_EXCEPTIONS()") {
                m.params.foreach(p => {
                  if (
                    isInterface(
                      p.ty.resolved
                    ) && spec.cppNnCheckExpression.nonEmpty
                  ) {
                    // We have a non-optional interface, assert that we're getting a non-null value
                    val paramName = idObjc.local(p.ident)
                    val stringWriter = new StringWriter()
                    writeObjcFuncDecl(m, new IndentWriter(stringWriter))
                    val singleLineFunctionDecl =
                      stringWriter.toString.replaceAll("\n *", " ")
                    val exceptionReason =
                      s"Got unexpected null parameter '$paramName' to function $objcSelf $singleLineFunctionDecl"
                    w.w(s"if ($paramName == nil)").braced {
                      w.wl(
                        s"""throw std::invalid_argument("$exceptionReason");"""
                      )
                    }
                  }
                })
                val ret = m.ret.fold("")(_ => "auto objcpp_result_ = ")
                val call = ret + (if (!m.static) "_cppRefHandle.get()->"
                                  else cppSelf + "::") + idCpp
                  .method(m.ident) + "("
                writeAlignedCall(
                  w,
                  call,
                  m.params,
                  ")",
                  p => objcppMarshal.toCpp(p.ty, idObjc.local(p.ident.name))
                )

                w.wl(";")
                m.ret.fold()(r =>
                  w.wl(s"return ${objcppMarshal.fromCpp(r, "objcpp_result_")};")
                )
              }
            }
          }

          if (i.consts.nonEmpty) {
            w.wl
            generateObjcConstants(
              w,
              i.consts,
              self,
              ObjcConstantType.ConstMethod
            )
          }
        }

        if (i.ext.objc) {
          w.wl
          wrapNamespace(
            w,
            spec.objcppNamespace,
            w => {
              w.wl(s"class $helperClass::ObjcProxy final")
              w.wl(s": public $cppSelf")
              w.wl(s", private ::djinni::ObjcProxyBase<ObjcType>")
              w.bracedSemi {
                w.wl(s"friend class ${objcppMarshal.helperClassWithNs(ident)};")
                w.wlOutdent("public:")
                w.wl("using ObjcProxyBase::ObjcProxyBase;")
                for (m <- i.methods) {
                  val ret = cppMarshal.fqReturnType(m.ret)
                  val params = m.params.map(p =>
                    cppMarshal.fqParamType(p.ty) + " c_" + idCpp.local(p.ident)
                  )
                  w.wl(
                    s"$ret ${idCpp.method(m.ident)}${params.mkString("(", ", ", ")")} override"
                  ).braced {
                    w.w("@autoreleasepool").braced {
                      val ret = m.ret.fold("")(_ => "auto objcpp_result_ = ")
                      val call =
                        s"[djinni_private_get_proxied_objc_object() ${idObjc
                          .method(m.ident)}"
                      writeAlignedObjcCall(
                        w,
                        ret + call,
                        m.params,
                        "]",
                        p =>
                          (
                            idObjc.field(p.ident),
                            s"(${objcppMarshal.fromCpp(p.ty, "c_" + idCpp.local(p.ident))})"
                          )
                      )
                      w.wl(";")
                      m.ret.fold()(ty => {
                        if (
                          spec.cppNnCheckExpression.nonEmpty && isInterface(
                            ty.resolved
                          )
                        ) {
                          // We have a non-optional interface, so assert that we're getting a non-null value
                          // before putting it into a non-null pointer
                          val stringWriter = new StringWriter()
                          writeObjcFuncDecl(m, new IndentWriter(stringWriter))
                          val singleLineFunctionDecl =
                            stringWriter.toString.replaceAll("\n *", " ")
                          val exceptionReason =
                            s"Got unexpected null return value from function $objcSelf $singleLineFunctionDecl"
                          w.w(s"if (objcpp_result_ == nil)").braced {
                            w.wl(
                              s"""throw std::invalid_argument("$exceptionReason");"""
                            )
                          }
                        }
                        w.wl(
                          s"return ${objcppMarshal.toCpp(ty, "objcpp_result_")};"
                        )
                      })
                    }
                  }
                }
              }
            }
          )
        }

        w.wl
        wrapNamespace(
          w,
          spec.objcppNamespace,
          w => {
            // ObjC-to-C++ coversion
            w.wl(s"auto $helperClass::toCpp(ObjcType objc) -> CppType").braced {
              // Handle null
              w.w("if (!objc)").braced {
                if (spec.cppNnType.isEmpty) {
                  w.wl("return nullptr;")
                } else {
                  w.wl(
                    s"""throw std::invalid_argument("$helperClass::toCpp requires non-nil object");"""
                  )
                }
              }
              if (i.ext.cpp && !i.ext.objc) {
                // C++ only. In this case we generate a class instead of a protocol, so
                // we don't have to do any casting at all, just access cppRef directly.
                w.wl("return " + nnCheck("objc->_cppRefHandle.get()") + ";")
                //w.wl(s"return ${spec.cppNnCheckExpression.getOrElse("")}(objc->_cppRefHandle.get());")
              } else if (i.ext.cpp || i.ext.objc) {
                // ObjC only, or ObjC and C++.
                if (i.ext.cpp) {
                  // If it could be implemented in C++, we might have to unwrap a proxy object.
                  w.w(s"if ([(id)objc isKindOfClass:[$objcSelf class]])")
                    .braced {
                      val getProxyExpr =
                        s"(($objcSelf*)objc)->_cppRefHandle.get()"
                      w.wl(s"return ${nnCheck(getProxyExpr)};")
                    }
                }
                val getProxyExpr = s"::djinni::get_objc_proxy<ObjcProxy>(objc)"
                w.wl(s"return ${nnCheck(getProxyExpr)};")
              } else {
                // Neither ObjC nor C++.  Unusable, but generate compilable code.
                w.wl(
                  "DJINNI_UNIMPLEMENTED(@\"Interface not implementable in any language.\");"
                )
              }
            }
            w.wl
            w.wl(
              s"auto $helperClass::fromCppOpt(const CppOptType& cpp) -> ObjcType"
            ).braced {
              // Handle null
              w.w("if (!cpp)").braced {
                w.wl("return nil;")
              }
              if (i.ext.objc && !i.ext.cpp) {
                // ObjC only. In this case we *must* unwrap a proxy object - the dynamic_cast will
                // throw bad_cast if we gave it something of the wrong type.
                w.wl(
                  s"return dynamic_cast<ObjcProxy&>(*cpp).djinni_private_get_proxied_objc_object();"
                )
              } else if (i.ext.objc || i.ext.cpp) {
                // C++ only, or C++ and ObjC.
                if (i.ext.objc) {
                  // If it could be implemented in ObjC, we might have to unwrap a proxy object.
                  w.w(s"if (auto cppPtr = dynamic_cast<ObjcProxy*>(cpp.get()))")
                    .braced {
                      w.wl(
                        "return cppPtr->djinni_private_get_proxied_objc_object();"
                      )
                    }
                }
                w.wl(s"return ::djinni::get_cpp_proxy<::$objcSelf>(cpp);")
              } else {
                // Neither ObjC nor C++.  Unusable, but generate compilable code.
                w.wl(
                  "DJINNI_UNIMPLEMENTED(@\"Interface not implementable in any language.\");"
                )
              }
            }
          }
        )

        if (i.ext.cpp) {
          w.wl
          w.wl("@end")
        }
      }
    )
  }

  override def generateRecord(
      origin: String,
      ident: Ident,
      doc: Doc,
      params: Seq[TypeParam],
      r: Record
  ) {
    val refs = new ObjcRefs()
    for (c <- r.consts)
      refs.find(c.ty)
    for (f <- r.fields)
      refs.find(f.ty)

    val objcName = ident.name + (if (r.ext.objc) "_base" else "")
    val noBaseSelf = objcMarshal.typename(ident, r) // Used for constant names
    val cppSelf = cppMarshal.fqTypename(ident, r)

    refs.privHeader.add(
      "!#import " + q(
        (if (r.ext.objc) spec.objcExtendedRecordIncludePrefix
         else spec.objcppIncludeObjcPrefix) + headerName(ident)
      )
    )
    refs.privHeader.add(
      "!#include " + q(
        (if (r.ext.cpp) spec.cppExtendedRecordIncludePrefix
         else spec.objcppIncludeCppPrefix) + spec.cppFileIdentStyle(
          ident
        ) + "." + spec.cppHeaderExt
      )
    )

    refs.body.add("#include <cassert>")
    refs.body.add(
      "!#import " + q(
        spec.objcppIncludePrefix + objcppMarshal.privateHeaderName(objcName)
      )
    )

    def checkMutable(tm: MExpr): Boolean = tm.base match {
      case MOptional => checkMutable(tm.args.head)
      case MString   => true
      case MBinary   => true
      case _         => false
    }

    val helperClass = objcppMarshal.helperClass(ident)

    writeObjcFile(
      objcName,
      isHeader = true,
      origin,
      refs.privHeader,
      w => {
        arcAssert(w)
        w.wl
        w.wl(s"@class $noBaseSelf;")
        w.wl
        wrapNamespace(
          w,
          spec.objcppNamespace,
          w => {
            w.wl(s"struct $helperClass")
            w.bracedSemi {
              w.wl(s"using CppType = $cppSelf;")
              w.wl(s"using ObjcType = ::$noBaseSelf*;")
              w.wl
              w.wl(s"using Boxed = $helperClass;")
              w.wl
              w.wl(s"static CppType toCpp(ObjcType objc);")
              w.wl(s"static ObjcType fromCpp(const CppType& cpp);")
            }
          }
        )
      }
    )

    writeObjcFile(
      objcName,
      isHeader = false,
      origin,
      refs.body,
      w => {
        wrapNamespace(
          w,
          spec.objcppNamespace,
          w => {
            w.wl(s"auto $helperClass::toCpp(ObjcType obj) -> CppType")
            w.braced {
              w.wl("assert(obj);")
              if (r.fields.isEmpty)
                w.wl(
                  "(void)obj; // Suppress warnings in relase builds for empty records"
                )
              val call = "return CppType("
              writeAlignedCall(
                w,
                "return {",
                r.fields,
                "}",
                f => objcppMarshal.toCpp(f.ty, "obj." + idObjc.field(f.ident))
              )
              w.wl(";")
            }
            w.wl
            w.wl(s"auto $helperClass::fromCpp(const CppType& cpp) -> ObjcType")
            w.braced {
              if (r.fields.isEmpty)
                w.wl(
                  "(void)cpp; // Suppress warnings in relase builds for empty records"
                )
              val first =
                if (r.fields.isEmpty) ""
                else IdentStyle.camelUpper("with_" + r.fields.head.ident.name)
              val call = s"return [[::$noBaseSelf alloc] init$first"
              writeAlignedObjcCall(
                w,
                call,
                r.fields,
                "]",
                f =>
                  (
                    idObjc.field(f.ident),
                    s"(${objcppMarshal.fromCpp(f.ty, "cpp." + idCpp.field(f.ident))})"
                  )
              )
              w.wl(";")
            }
          }
        )
      }
    )
  }

  def writeObjcFile(
      objcName: String,
      isHeader: Boolean,
      origin: String,
      refs: Iterable[String],
      f: IndentWriter => Unit
  ) {
    val folder =
      if (isHeader) spec.objcppHeaderOutFolder else spec.objcppOutFolder
    val fileName =
      if (isHeader) objcppMarshal.privateHeaderName(objcName)
      else privateBodyName(objcName)
    createFile(
      folder.get,
      fileName,
      (w: IndentWriter) => {
        w.wl("// AUTOGENERATED FILE - DO NOT MODIFY!")
        w.wl("// This file was generated by Djinni from " + origin)
        w.wl
        if (refs.nonEmpty) {
          var included = mutable.TreeSet[String]()
          for (s <- refs) {
            // Ignore the ! in front of each line; used to put own headers to the top
            // according to Objective-C style guide
            val include = if (s.charAt(0) == '!') s.substring(1) else s
            if (!included.contains(include)) {
              included += include
              w.wl(include)
            }
          }
          w.wl
        }
        f(w)
      }
    )
  }
}
