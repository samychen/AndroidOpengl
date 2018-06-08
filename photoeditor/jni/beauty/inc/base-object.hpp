#ifndef SAK_PLATFORM_BASE_OBJECT_HPP__
#define SAK_PLATFORM_BASE_OBJECT_HPP__

#include "tmem.h"
#include <new>
#include <cassert>

/**
 * Warpers for BaseObject::new/delete operators.
 */
#  define sakNew(pool, A_CLASS)             (new(pool) A_CLASS)

template <class T>
void sakDelete(void* pool, void* p)
{
    T::operator delete(p, pool);
}

#  define sakNewArray(pool, A_CLASS, size)  (new(pool) A_CLASS[size])

template <class T>
void sakDeleteArray(void* pool, void*p)
{
    T::operator delete[](p, pool);
}


namespace sak {

inline TVoid*	TMemArrayAlloc(THandle hContext, TLong lSize, int count)
{
    TVoid* p = TMemAlloc(hContext, lSize+sizeof(int));
    long* q = (long*)p;
    *q = count;
    q++;
    return q;
}

inline long     TMemArrayCount(THandle hContext, TVoid* p)
{
    long* q = (long*)p;
    return *(q-1);
}

inline TVoid	TMemArrayFree(THandle hContext, TVoid* pMem)
{
    long*q = (long*)pMem;
    TMemFree(hContext, q-1);
}

template <class T>
class BaseObject
{
public:
    BaseObject() { _memHandle = 0; }
    BaseObject(THandle memHandle) { _memHandle = memHandle; }

    /**
     * Overloading new/delete operators
     */

    /// normal
    void* operator new (std::size_t size) throw(std::bad_alloc) { return ::operator new (size); }
    void  operator delete (void* p) throw() { if (p) ::operator delete(p); }
    void* operator new (std::size_t size, const std::nothrow_t&) throw(std::bad_alloc) { return ::operator new (size, std::nothrow); }
    void  operator delete (void* p, std::nothrow_t&) { if (p) ::operator delete(p, std::nothrow); }

    /// array
    void* operator new[]    (std::size_t size) throw(std::bad_alloc) { return ::operator new[] (size); }
    void  operator delete[] (void* p) throw() { if (p) ::operator delete[](p); }
    void* operator new[]    (std::size_t size, std::nothrow_t&) throw(std::bad_alloc) { return ::operator new[] (size, std::nothrow); }
    void  operator delete[] (void* p, std::nothrow_t&) throw() { if (p) ::operator delete[](p, std::nothrow); }

    /// placement
    void* operator new    (std::size_t bytes, void* pool) throw (std::bad_alloc)
        {
            return TMemAlloc((THandle)pool, bytes);
        }
    void  operator delete (void* p, void* pool) throw()
        {
            ((T*)p)->~T();
            TMemFree((THandle)pool, p);
        }
    void* operator new[]    (std::size_t bytes, void* pool) throw (std::bad_alloc)
        {
            return TMemArrayAlloc((THandle)pool, bytes, bytes/sizeof(T));
        }
    void  operator delete[] (void* p, void* pool) throw()
        {
            T* q = (T*)p;
            long n = TMemArrayCount((THandle)pool, p);
            assert(n>=0);
            while (n-- > 0) {
                q->~T();
                ++q;
            }
            TMemArrayFree((THandle)pool, p);
        }

    /**
     * Properties
     */
    void    setMemHandle(THandle memHandle) { _memHandle = memHandle; }
    THandle memHandle() { return _memHandle; }

private:
    THandle  _memHandle;
};

}

#endif//SAK_PLATFORM_BASE_OBJECT_HPP__
