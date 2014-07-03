
#include <stdio.h>
#include <stdlib.h>

#include <errno.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <string.h>
#include <sys/times.h>

#include <semaphore.h>
#include <pthread.h>

#include <jni.h>
#include <android/log.h>


#define PHILOSOPHERS_METHOD(name) Java_pl_edu_agh_informatyka_so_threadsdemo_PhilosophersActivity_##name
#define READERSWRITERS_METHOD(name) Java_pl_edu_agh_informatyka_so_threadsdemo_ReadersWritersActivity_##name
#define SLEEPINGBARBER_METHOD(name) Java_pl_edu_agh_informatyka_so_threadsdemo_SleepingBarberActivity_##name
#define PRODUCERSCONSUMERS_METHOD(name) Java_pl_edu_agh_informatyka_so_threadsdemo_ProducersConsumersActivity_##name

#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO, "Thread", __VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR, "Thread", __VA_ARGS__)


#define SIZE 5

int IDs[SIZE];
pthread_t philosophers[SIZE];
pthread_mutex_t forks[SIZE];
sem_t canteen;

//int is_eating[SIZE];
double eating_time[SIZE];
int who_has_fork[SIZE];

int phil_active;
int rw_active;
int barb_active;
int pc_active;

jobject phil_lsnr;
jobject rw_lsnr;
jobject barb_lsnr;
jobject pc_lsnr;

JavaVM* gJVM;
jobject table_state_class, bookshop_state_class, barbershop_state_class;

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *jvm, void *reserved)
{
	gJVM = jvm;

	JNIEnv *env;
	(*gJVM)->GetEnv(gJVM, (void**)&env, JNI_VERSION_1_6);
	
	table_state_class = (*env)->NewGlobalRef(env,
		(*env)->FindClass(env, "pl/edu/agh/informatyka/so/threadsdemo/TableState"));
		
	bookshop_state_class = (*env)->NewGlobalRef(env,
		(*env)->FindClass(env, "pl/edu/agh/informatyka/so/threadsdemo/BookshopState"));
	
	barbershop_state_class = (*env)->NewGlobalRef(env,
		(*env)->FindClass(env, "pl/edu/agh/informatyka/so/threadsdemo/BarbershopState"));
		
	srand(time(NULL));
	
	return JNI_VERSION_1_6;
}

void notify_table_change_listener();

clock_t get_utime_as_clock() {
	struct tms buffer;
	times(&buffer);
	return buffer.tms_utime;
}

void pickup_fork(int philosopher_number, int fork_no) {
	if((errno = pthread_mutex_lock(forks + fork_no)) != 0)
		LOGE("in take_fork() --> function pthread_mutex_lock()");
	who_has_fork[fork_no] = philosopher_number;
}

void putdown_fork(int fork_no) {	
	who_has_fork[fork_no] = -1;
	if((errno = pthread_mutex_unlock(forks + fork_no)) != 0)
		LOGE("in function give_back_fork() --> pthread_mutex_unlock()");
}

void sleep_while_active(int millisecs) {
	int i;
	for (i = 0; i < millisecs && phil_active; i++)
		usleep(1000000);
}

void* thread_function(void* arg){

	JNIEnv *env;
	
	(*gJVM)->AttachCurrentThread(gJVM, &env, NULL);
	
	int philosopher_number = *((int*) arg);
	int left_fork = philosopher_number;
	int right_fork = (philosopher_number + 1) % 5;
	
	while (phil_active) {
		// printf("Philosopher #%d is thinking.\n", philosopher_number);
		usleep(rand() % 100);
		
		/*if(philosopher_number % 2) {		
			pickup_fork(philosopher_number, left_fork);		
			pickup_fork(philosopher_number, right_fork);
		}													
		else {
			pickup_fork(philosopher_number, right_fork);		
			pickup_fork(philosopher_number, left_fork);					
		}*/
		
		sem_wait(&canteen);
		pickup_fork(philosopher_number, left_fork);		
		pickup_fork(philosopher_number, right_fork);
			
		//is_eating[philosopher_number] = 1;
		//clock_t start_utime = get_utime_as_clock();
		
		notify_table_change_listener(env);
		
		//sleep_while_active(rand() % 10);		
		int i, units = (3 + rand() % 5) * 2;
		for (i = 0; i < units && phil_active; i++) {
			usleep(500000);
			eating_time[philosopher_number] += 0.5;
			notify_table_change_listener(env);
		}
		
		//clock_t end_utime = get_utime_as_clock();
		//eating_time[philosopher_number] += (double)(end_utime - start_utime);
		
		//is_eating[philosopher_number] = 0;
				
		putdown_fork(left_fork);
		putdown_fork(right_fork);
		sem_post(&canteen);
		
		//LOGI("In thread_function, before notify for putdown");
		notify_table_change_listener(env);
		//LOGI("In thread_function, after notify for putdown");
	}

	(*gJVM)->DetachCurrentThread(gJVM);
	//LOGE("In thread_function, detached from JVM");
	return NULL;
}

void PHILOSOPHERS_METHOD(startTable) (JNIEnv* env, jobject thiz)
{
	phil_active = 1;
	phil_lsnr = (*env)->NewGlobalRef(env, thiz);	
	
	pthread_mutexattr_t attr;
	if((errno = pthread_mutexattr_init(&attr)) > 0)
		LOGE("in function pthread_mutex_init()");
	if((errno = pthread_mutexattr_settype(&attr, PTHREAD_MUTEX_NORMAL)) > 0)
		LOGE("in function pthread_mutex_init()");

	int i;	
	for (i = 0; i < SIZE; ++i){
		IDs[i] = i;
		//is_eating[i] = 0;	
		who_has_fork[i] = -1;
		eating_time[i] = 0.0;
		if((errno = pthread_mutex_init(forks + i, &attr)) > 0)
			LOGE("in function pthread_mutex_init()");
	}
	
	sem_init(&canteen, 0, SIZE - 1);

	for (i = 0; i < SIZE; ++i){
		if((errno = pthread_create(philosophers + i, NULL, thread_function, IDs + i)) != 0)
			LOGE("in function pthread_create()");
	}
}

void PHILOSOPHERS_METHOD(stopTable) (JNIEnv* env, jobject thiz) {
	phil_active = 0;	
	int i;
	for (i = 0; i < SIZE; ++i){
		if((errno = pthread_join(philosophers[i], NULL)) != 0) {			
			char log[1000];
			sprintf(log, "in function stopTable: %s", strerror(errno));
			LOGI(log);
		}
	}
	(*env)->DeleteGlobalRef(env, phil_lsnr);
}

jintArray get_wrapped_int_array (JNIEnv* env, int* array, int size) {
	jintArray res = (*env)->NewIntArray(env, (jint)size);
	if (res == NULL)
			return NULL;
	
	(*env)->SetIntArrayRegion(env, res, 0, size, array);
	return res;
}

jdoubleArray get_wrapped_double_array (JNIEnv* env, double* array, int size) {
	jdoubleArray res = (*env)->NewDoubleArray(env, (jint)size);
	if (res == NULL)
			return NULL;
	
	(*env)->SetDoubleArrayRegion(env, res, 0, size, array);
	return res;
}


jobject get_table_state (JNIEnv* env, jobject thiz) {

	jmethodID constr = (*env)->GetMethodID(env, table_state_class, "<init>", "([I[D)V");
	
	jintArray jForkOwner = get_wrapped_int_array(env, who_has_fork, SIZE);
	jdoubleArray jEatingTime = get_wrapped_double_array(env, eating_time, SIZE);
	
	jobject res = (*env)->NewObject(env, table_state_class, constr, jForkOwner, jEatingTime);
	
	(*env)->DeleteLocalRef(env, jForkOwner);
	(*env)->DeleteLocalRef(env, jEatingTime);
	
	return res;
}

void notify_table_change_listener (JNIEnv* listener_env) {
	if (phil_active) {
		
		jclass clazz = (*listener_env)->GetObjectClass(listener_env, phil_lsnr);
		//jclass clazz = (*listener_env)->FindClass(listener_env, "pl/edu/agh/informatyka/so/threadsdemo/PhilosophersDetailFragment");
		
		jobject state = get_table_state(listener_env, phil_lsnr);	
		
		jmethodID method = (*listener_env)->GetMethodID(listener_env, clazz, 
			"tableStateChanged", "(Lpl/edu/agh/informatyka/so/threadsdemo/TableState;)V");
		(*listener_env)->DeleteLocalRef(listener_env, clazz);
		
		(*listener_env)->CallVoidMethod(listener_env, phil_lsnr, method, state);
		(*listener_env)->DeleteLocalRef(listener_env, state);
	}
}


/*** Readers-writers ***/

sem_t mutex, writeblock;
int data = 0, rcount = 0;

int readers_cnt, writers_cnt;
pthread_t *readers, *writers;

int* is_reading;
int current_writer;

void notify_bookshop_change_listener(JNIEnv* listener_env);

void rw_sleep(int secs) {
	int i;
	for (i = 0; i < secs && rw_active; i++)
		usleep(1000000);
}

void *reader_fun(void *arg)
{
	JNIEnv *env;
	(*gJVM)->AttachCurrentThread(gJVM, &env, NULL);
	
	int no = ((int)arg);

	while (rw_active) {
		sem_wait(&mutex);
		if(++rcount==1)
			sem_wait(&writeblock);
		sem_post(&mutex);

		//printf("Data read by the reader%d is %d\n",f,data);
		is_reading[no] = 1;
		notify_bookshop_change_listener(env);
				
		rw_sleep(rand() % 3);
		//usleep(1000000);
		
		is_reading[no] = 0;
		notify_bookshop_change_listener(env);

		sem_wait(&mutex);
		if(--rcount==0)
			sem_post(&writeblock);
		sem_post(&mutex);
				
		rw_sleep(rand() % 10);
	}
	
	(*gJVM)->DetachCurrentThread(gJVM);

	return NULL;
}

void *writer_fun(void *arg)
{
	JNIEnv *env;
	(*gJVM)->AttachCurrentThread(gJVM, &env, NULL);
	
	while (rw_active) {
		sem_wait(&writeblock);
		
		current_writer = ((int) arg);
		notify_bookshop_change_listener(env);
		
		data++;
		//printf("Data writen by the writer%d is %d\n",f,data);
		//usleep(1000000);
		rw_sleep(rand() % 3);
		
		current_writer = -1;
		notify_bookshop_change_listener(env);
		
		sem_post(&writeblock);
		
		//usleep(1000*(rand()%5000));
		/*int secs = rand() % 10, i;
		for (i = 0; i < secs && rw_active; i++)
			usleep(1000000);*/
		rw_sleep(rand() % 10);
	}	
	
	(*gJVM)->DetachCurrentThread(gJVM);
	
	return NULL;
}

void READERSWRITERS_METHOD(startBookshop) (JNIEnv* env, jobject thiz, jint _readers_cnt, jint _writers_cnt) {

	rw_active = 1;
	rw_lsnr = (*env)->NewGlobalRef(env, thiz);
	
	readers_cnt = _readers_cnt;
	readers = malloc(sizeof(pthread_t) * readers_cnt);
	is_reading = malloc(sizeof(int) * readers_cnt);
	memset(is_reading, 0, sizeof(int) * readers_cnt);
	
	writers_cnt = _writers_cnt;
	writers = malloc(sizeof(pthread_t) * writers_cnt);
	current_writer = -1;

	sem_init(&mutex, 0, 1);
	sem_init(&writeblock, 0, 1);

	int i;
	for (i = 0; i < readers_cnt; i++) {   
		pthread_create(&readers[i], NULL, reader_fun, (void *)i);
	}

	for (i = 0; i < writers_cnt; i++) {   
		pthread_create(&writers[i], NULL, writer_fun, (void *)i);
	}
}

void READERSWRITERS_METHOD(stopBookshop) (JNIEnv* env, jobject thiz) {
	rw_active = 0;
	
	int i;
	for (i = 0; i < readers_cnt; i++)
		pthread_join(readers[i], NULL);
	
	for (i = 0; i < writers_cnt; i++)
		pthread_join(writers[i], NULL);
		
	free(readers);
	free(is_reading);
	free(writers);
	
	(*env)->DeleteGlobalRef(env, rw_lsnr);
}

jobject get_bookshop_state(JNIEnv* env, jobject thiz) {

	jmethodID constr_writer = (*env)->GetMethodID(env, bookshop_state_class, "<init>", "(II[II)V");
	
	jint jreaderCnt = readers_cnt;
	jint jwriterCnt = writers_cnt;
	jintArray jIsReading = get_wrapped_int_array(env, is_reading, readers_cnt);
	jint jcurrentWriter = current_writer;
	
	jobject res = (*env)->NewObject(env, bookshop_state_class, 
		constr_writer, jreaderCnt, jwriterCnt, jIsReading, jcurrentWriter);
	
	(*env)->DeleteLocalRef(env, jIsReading);
	return res;
}

void rw_dbg_report() {
	char buf[100];
	int i;
	for (i = 0; i < readers_cnt; i++)
		if (is_reading[i]) {
			sprintf(buf, "Active reader: %d", i);
			LOGE(buf);
		}
		
	if (current_writer >= 0) {
		sprintf(buf, "Active writer: %d", i);
		LOGE(buf);
	}
}

void notify_bookshop_change_listener(JNIEnv* listener_env) {
	if (rw_active) {
		
		//rw_dbg_report();
		jclass clazz = (*listener_env)->GetObjectClass(listener_env, rw_lsnr);
		//jclass clazz = (*listener_env)->FindClass(listener_env, "pl/edu/agh/informatyka/so/threadsdemo/PhilosophersDetailFragment");
		
		jobject state = get_bookshop_state(listener_env, rw_lsnr);	
		
		jmethodID method = (*listener_env)->GetMethodID(listener_env, clazz, 
			"bookshopStateChanged", "(Lpl/edu/agh/informatyka/so/threadsdemo/BookshopState;)V");
		(*listener_env)->DeleteLocalRef(listener_env, clazz);
		
		(*listener_env)->CallVoidMethod(listener_env, rw_lsnr, method, state);
		(*listener_env)->DeleteLocalRef(listener_env, state);
	}
}



/*** Sleeping Barber ***/

#define _REENTRANT

// Function prototypes...
void *customer_fun(void *num);
void *barber_fun(void *);

void randwait(int secs);

// waitingRoom Limits the # of customers allowed 
// to enter the waiting room at one time.
sem_t waitingRoom;   

// barberChair ensures mutually exclusive access to
// the barber chair.
sem_t barberChair;

// barberPillow is used to allow the barber to sleep
// until a customer arrives.
sem_t barberPillow;

// seatBelt is used to make the customer to wait until
// the barber is done cutting his/her hair. 
sem_t seatBelt;

int customers_cnt, chairs_cnt;
pthread_t barber;
pthread_t* customers;
int* is_sitting;
int* is_waiting;
int current_customer;

void SLEEPINGBARBER_METHOD(startBarbershop) (JNIEnv* env, jobject thiz, jint _chairs_cnt, jint _customers_cnt) {

	barb_active = 1;
	barb_lsnr = (*env)->NewGlobalRef(env, thiz);
	
	chairs_cnt = _chairs_cnt;
	customers_cnt = _customers_cnt;
	customers = malloc(sizeof(pthread_t) * customers_cnt);
	is_sitting = malloc(sizeof(int) * customers_cnt);
	is_waiting = malloc(sizeof(int) * customers_cnt);
	memset(is_sitting, 0, sizeof(int) * customers_cnt);
		
	current_customer = -1;
      
    sem_init(&waitingRoom, 0, chairs_cnt);
    sem_init(&barberChair, 0, 1);
    sem_init(&barberPillow, 0, 0);
    sem_init(&seatBelt, 0, 0);
        
    pthread_create(&barber, NULL, barber_fun, NULL);
    
	int i;
    for (i = 0; i < customers_cnt; i++)
		pthread_create(&customers[i], NULL, customer_fun, (void*)i);   
}

void SLEEPINGBARBER_METHOD(stopBarbershop) (JNIEnv* env, jobject thiz) {

	barb_active = 0;
	LOGE("Entered stop");
	int i;
	for (i = 0; i < customers_cnt; i++) {
		pthread_join(customers[i], NULL);
		LOGE("Finished a customer");
    }
	
    sem_post(&barberPillow);  // Wake the barber so he will exit.
    pthread_join(barber, NULL);		
	LOGE("Finished the barber");
		
	free(customers);
	free(is_sitting);
	free(is_waiting);
	
	(*env)->DeleteGlobalRef(env, barb_lsnr);
}

jobject get_barbershop_state(JNIEnv* env, jobject thiz) {

	jmethodID constr = (*env)->GetMethodID(env, barbershop_state_class, "<init>", "(II[I[II)V");
	
	jint jchairCnt = chairs_cnt;	
	jint jcustomerCnt = customers_cnt;	
	jintArray jisWaiting = get_wrapped_int_array(env, is_waiting, customers_cnt);
	jintArray jisSitting = get_wrapped_int_array(env, is_sitting, customers_cnt);
	jint jcurrentCustomer = current_customer;
	
	jobject res = (*env)->NewObject(env, barbershop_state_class, 
		constr, jchairCnt, jcustomerCnt, jisWaiting, jisSitting, jcurrentCustomer);
	
	(*env)->DeleteLocalRef(env, jisWaiting);
	(*env)->DeleteLocalRef(env, jisSitting);
	return res;
}

void notify_barbershop_change_listener(JNIEnv* listener_env) {
	if (barb_active) {
		
		jclass clazz = (*listener_env)->GetObjectClass(listener_env, barb_lsnr);		
		
		jobject state = get_barbershop_state(listener_env, barb_lsnr);
		
		jmethodID method = (*listener_env)->GetMethodID(listener_env, clazz, 
			"barbershopStateChanged", "(Lpl/edu/agh/informatyka/so/threadsdemo/BarbershopState;)V");
		(*listener_env)->DeleteLocalRef(listener_env, clazz);
		
		(*listener_env)->CallVoidMethod(listener_env, barb_lsnr, method, state);
		(*listener_env)->DeleteLocalRef(listener_env, state);
	}
}

void barb_sleep(int secs) {
	int i;
	for (i = 0; i < secs && barb_active; i++)
		usleep(1000000);
}

int barb_wait(sem_t* sem) {
	while (barb_active) {		
		if(sem_trywait(sem) == 0)
			return 1;
	}
	return 0;
}

void *customer_fun(void *arg) {
	JNIEnv *env;
	(*gJVM)->AttachCurrentThread(gJVM, &env, NULL);
	
    int no = (int)arg;
	
	barb_sleep(1 + rand() % 8);
	
	while (barb_active) {    
		
		is_waiting[no] = 1;
		notify_barbershop_change_listener(env);
		
		LOGE("Customer left for the barbershop.\n");
		if (!barb_wait(&waitingRoom))
			break;
			
		//sem_wait(&waitingRoom);
		is_sitting[no] = 1;
		notify_barbershop_change_listener(env);
		LOGE("Customer entered waiting room.\n");

		// Wait for the barber chair to become free.
		//sem_wait(&barberChair);
		if (!barb_wait(&barberChair))
			break;
		
		// The chair is free so give up your spot in the
		// waiting room.
		sem_post(&waitingRoom);

		// Wake up the barber...
		//printf("Customer %d waking the barber.\n", num);
		sem_post(&barberPillow);
		
		current_customer = no;
		is_sitting[no] = 0;
		is_waiting[no] = 0;
		notify_barbershop_change_listener(env);
		// Wait for the barber to finish cutting your hair.
		LOGE("Customer waiting for hair to cut.\n");
		sem_wait(&seatBelt);
		//if (!barb_wait(&seatBelt))
		//	break;
		
		current_customer = -1;		
		// Give up the chair.
		sem_post(&barberChair);
		LOGE("Customer leaving barber shop.\n");
		
		barb_sleep(4 + rand() % 5);    
	}
	
	(*gJVM)->DetachCurrentThread(gJVM);
	return NULL;
}

void *barber_fun(void *junk) {
    // While there are still customers to be serviced...
    // Our barber is omnicient and can tell if there are 
    // customers still on the way to his shop.
    while (barb_active) {

		// Sleep until someone arrives and wakes you..
		LOGE("The barber is sleeping\n");
		//sem_wait(&barberPillow);
		if (!barb_wait(&barberPillow))
			break;
		
		//if (barb_active) {

		// Take a random amount of time to cut the
		// customer's hair.
		LOGE("The barber is cutting hair\n");
		barb_sleep(1 + rand() % 3);
		LOGE("The barber has finished cutting hair.\n");

		// Release the customer when done cutting...
		sem_post(&seatBelt);
	}
	return NULL;
}





/*** Producers & Consumers ***/


int producers_task_no;
int consumers_task_no;

int producers_cnt, consumers_cnt, products_cnt;
sem_t producers_sem, consumers_sem;

int *slot_free, *being_issued, *being_collected;
pthread_mutex_t* product_mutex;

pthread_t *consumers, *producers;

void *producer_fun(void *arg);
void *consumer_fun(void *arg);

void PRODUCERSCONSUMERS_METHOD(startFactory) (JNIEnv* env, jobject thiz, jint _producers_cnt, jint _consumers_cnt, jint _products_cnt) {

	pc_active = 1;
	pc_lsnr = (*env)->NewGlobalRef(env, thiz);
	
	producers_cnt = _producers_cnt;
	consumers_cnt = _consumers_cnt;
	products_cnt = _products_cnt;

	sem_init(&producers_sem, 0, products_cnt);
	sem_init(&consumers_sem, 0, 0);

	producers_task_no = 0;
	consumers_task_no = 0;
		
	producers = malloc(sizeof(pthread_t) * producers_cnt);
	consumers = malloc(sizeof(pthread_t) * consumers_cnt);
	
	slot_free = malloc(sizeof(int) * products_cnt);
	being_issued = malloc(sizeof(int) * products_cnt);
	being_collected = malloc(sizeof(int) * products_cnt);
    product_mutex = malloc(sizeof(pthread_mutex_t) * products_cnt);
	
	int i;
	
	for (i = 0; i < products_cnt; i++) {
		slot_free[i] = 1;
		being_collected[i] = 0;
		being_issued[i] = 0;
		pthread_mutex_init(&product_mutex[i], NULL);		
	}
	
    for (i = 0; i < producers_cnt; i++)
		pthread_create(&producers[i], NULL, producer_fun, (void*)i);
		
	for (i = 0; i < consumers_cnt; i++)
		pthread_create(&consumers[i], NULL, consumer_fun, (void*)i);
}

void PRODUCERSCONSUMERS_METHOD(stopFactory) (JNIEnv* env, jobject thiz) {

	pc_active = 0;
	
	int i;
	for (i = 0; i < producers_cnt; i++)
		pthread_join(producers[i], NULL);
		
	for (i = 0; i < consumers_cnt; i++)
		pthread_join(consumers[i], NULL);
		
	free(producers);
	free(consumers);	
	free(slot_free);
	free(being_issued);
	free(being_collected);
	free(product_mutex);
	
	(*env)->DeleteGlobalRef(env, pc_lsnr);
}

void PRODUCERSCONSUMERS_METHOD(notifyProductIssued) (JNIEnv* env, jobject thiz, int slotno) {
	being_issued[slotno] = 0;
}

void PRODUCERSCONSUMERS_METHOD(notifyProductCollected) (JNIEnv* env, jobject thiz, int slotno) {
	being_collected[slotno] = 0;
	slot_free[slotno] = 1;
	
	char log[100];
	sprintf(log, "notifyProductCollected - %i", slotno);
	LOGE(log);
	
	/*int i;
	for (i = 0; i < products_cnt; i++) {
		char log[100];
		sprintf(log, "(notifyProductCollected) Product $%i - free = %i, issuing = %i, collecting = %i\n", i, slot_free[i], being_issued[i], being_collected[i]);
182		LOGE(log);
	}*/
}

void notify_factory_production_listener(JNIEnv* listener_env, int prodno, int slotno) {
	if (pc_active) {
		
		jclass clazz = (*listener_env)->GetObjectClass(listener_env, pc_lsnr);
				
		jmethodID method = (*listener_env)->GetMethodID(listener_env, clazz, "productIssued", "(II)V");
		(*listener_env)->DeleteLocalRef(listener_env, clazz);
		
		(*listener_env)->CallVoidMethod(listener_env, pc_lsnr, method, (jint)prodno, (jint)slotno);
	}
}

void notify_factory_consumption_listener(JNIEnv* listener_env, int consno, int slotno) {
	if (pc_active) { 	
		
		jclass clazz = (*listener_env)->GetObjectClass(listener_env, pc_lsnr);
				
		jmethodID method = (*listener_env)->GetMethodID(listener_env, clazz, "productCollected", "(II)V");
		(*listener_env)->DeleteLocalRef(listener_env, clazz);
		
		(*listener_env)->CallVoidMethod(listener_env, pc_lsnr, method, (jint)consno, (jint)slotno);
	}
}

void pc_sleep(int secs) {
	int i;
	for (i = 0; i < secs && pc_active; i++)
		usleep(1000000);
}

int issue_into_free_slot() {
	//LOGE("issue called");
	int i;
	for (i = 0; pc_active && i < products_cnt; i++) {
		//char log[100];
		//sprintf(log, "(issue) Checking product $%i - free = %i, issuing = %i, collecting = %i\n", i, slot_free[i], being_issued[i], being_collected[i]);
		//LOGE(log);
		
		//sprintf(log, "(issue) Waiting on mutex for product %i\n", i);
		//LOGE(log);
		
		pthread_mutex_lock(&product_mutex[i]);
		
		//sprintf(log, "(issue) Acquired mutex for product %i\n", i);
		//LOGE(log);
		
		if (slot_free[i]) {
			slot_free[i] = 0;
			being_issued[i] = 1;
						
			//sprintf(log, "Issuing into the pos %d - now is occupied\n", i);
			//LOGE(log);
			
			//sprintf(log, "Product %i will be issued shortly\n", i);
			//LOGE(log);
		
			pthread_mutex_unlock(&product_mutex[i]);
			return i;
		}
		
		//sprintf(log, "(issue) Released mutex for product %i\n", i);
		//LOGE(log);
		
		pthread_mutex_unlock(&product_mutex[i]);
	}
	return -1;
}

void *producer_fun(void *arg) {
	JNIEnv *env;
	(*gJVM)->AttachCurrentThread(gJVM, &env, NULL);
	
    int no = (int)arg;
	
	while (pc_active) {
		//sem_wait(&producers_sem);
		
		int slotno = issue_into_free_slot();
		
		if (slotno >= 0) {
			char log[100];
			sprintf(log, "Producer %d issued a product on the pos %d\n", no, slotno);
			LOGE(log);
			
			notify_factory_production_listener(env, no, slotno);
			
			//producers_task_no = (producers_task_no + 1) % products_cnt;			
			
			//sem_post(&consumers_sem);					
		}
		
		pc_sleep(4 + rand() % 5);
	}

	(*gJVM)->DetachCurrentThread(gJVM);
	return NULL;
}

/*void collect_task(int c) {	
	//printf("#%d macierz (pozycja %d):\twyznacznik = %lf\n", 
	//	(*cons_tasks_total)++, *consumers_task_no, det);
	
	char log[100];
	sprintf(log, "Consumer %d collected a product from the pos %d\n", c, consumers_task_no);
	LOGE(log);
	
}*/

int collect_from_occupied_slot() {
	int i;
	for (i = 0; pc_active && i < products_cnt; i++) {
	
		//char log[100];
		//sprintf(log, "(collect) Checking product $%i - free = %i, issuing = %i, collecting = %i\n", i, slot_free[i], being_issued[i], being_collected[i]);
		//LOGE(log);
		
		//sprintf(log, "(collect) Waiting on mutex for product %i\n", i);
		//LOGE(log);
				
		pthread_mutex_lock(&product_mutex[i]);
		
		//sprintf(log, "(collect) Acquired mutex for product %i\n", i);
		//LOGE(log);
				
		
		if (!slot_free[i] && !being_issued[i] && !being_collected[i]) {
			being_collected[i] = 1;
						
			//sprintf(log, "Collecting from the pos %d - now is being consumed\n", i);
			//LOGE(log);
		
			//sprintf(log, "(collect) Released mutex (found!) for product %i\n", i);
			//LOGE(log);
		
			pthread_mutex_unlock(&product_mutex[i]);		
			return i;
		}
		
		//sprintf(log, "(collect) Released mutex for product %i\n", i);
		//LOGE(log);
		
		pthread_mutex_unlock(&product_mutex[i]);		
	}
	return -1;
}

void *consumer_fun(void *arg) {
	JNIEnv *env;
	(*gJVM)->AttachCurrentThread(gJVM, &env, NULL);
	
    int no = (int)arg;
	
	while (pc_active)  {
		//sem_wait(&consumers_sem);
		
		int slotno = collect_from_occupied_slot();
		
		if (slotno >= 0) {
		
			notify_factory_consumption_listener(env, no, slotno);
			//consumers_task_no = (consumers_task_no + 1) % products_cnt;
			
			char log[100];
			sprintf(log, "Consumer %d collected a product from the pos %d\n", no, slotno);
			LOGE(log);
			
			//sem_post(&producers_sem);
		}
		
		pc_sleep(4 + rand() % 5);
	}
	
	(*gJVM)->DetachCurrentThread(gJVM);
	return NULL;
}
