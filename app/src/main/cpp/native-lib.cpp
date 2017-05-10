#include <jni.h>
#include <map>
#include<vector>
#include <cstdlib>
using namespace std;

inline int f(int x, int n);

int pollard(int);

int gcd(int,int);

bool isPrime(int);

JNIEXPORT jobject JNICALL
Java_ru_crimsonhouse_pfactor_MainActivity_factorize(JNIEnv *env, jobject instance, jint x) {
    // TODO
    map<int, int> result;
    while(x>0) {
        int divider = pollard(x);
        while(x%divider == 0) {
            x/=divider;
            result[divider]++;
        }

    }
    jobject tmap = env->AllocObject(env->FindClass("java/util/TreeMap<>"));
    for(auto entry: result){
        env->CallVoidMethod(tmap, env->GetMethodID(env->GetObjectClass(tmap), "put")), NULL);
    }
    return tmap;
}

int pollard(int num){
    vector<int> v;
    v.push_back(rand()%num);
    int d=0;
    while(d<1) {
        v.push_back(f(v[v.size() - 1], num));
        int i = (int) (rand() % v.size());
        int j = i / 2;
        d = gcd(num, (unsigned int) (v[i] - v[j]));
    }
    if(!isPrime(d)) return pollard(d);
    else return d;
}

inline int f(int x, int n){
    return (x*x - 1)%n;
}

int gcd(int a, int b){
    if(a<b){
        int t = a;
        a=b;
        b=t;
    }
    while(b>0){
        a=a%b;
        int t = a;
        a=b;
        b=t;
    }
    return a;
}


bool isPrime(int x){
    for(int i=2; (i<<1) <= x; i++){
        if(x%i == 0) return false;
    }
    return true;
}

JNIEXPORT jboolean JNICALL
Java_ru_crimsonhouse_pfactor_MainActivity_isPrime(JNIEnv *env, jobject instance, jint x) {
    return (jboolean)isPrime(x);
}