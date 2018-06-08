#ifndef __DES_H__
#define __DES_H__

#define PLAIN_FILE_OPEN_ERROR	 -1
#define KEY_FILE_OPEN_ERROR		 -2
#define CIPHER_FILE_OPEN_ERROR	 -3
#define INVALID_BUFFER_SIZE		 -4
#define OK						 0

typedef char ElemType;

#ifdef __cplusplus
extern "C" {
#endif

int DES_EncryptFile(char *plainFile, char *keyStr,char *cipherFile);
int DES_DecryptFile(char *cipherFile, char *keyStr,char *plainFile);
int DES_EncryptBuffer(char *cbPlain, char *keyStr,char *cbCipher, int nSize);
int DES_DecryptBuffer(char *cbCipher, char *keyStr,char *cbPlain, int nSize);

#ifdef __cplusplus
};
#endif

#endif /*__DES_H__*/
