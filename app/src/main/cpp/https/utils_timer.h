#ifndef UTILS_TIMER_H_
#define UTILS_TIMER_H_
#include "config.h"
#ifdef __cplusplus
extern "C" {
#endif
// Add the platform specific timer includes to define the Timer struct
/**
 * @brief Check if a timer is expired
 *
 * Call this function passing in a timer to check if that timer has expired.
 *
 * @param timer - pointer to the timer to be checked for expiration
 * @return bool - true = timer expired, false = timer not expired
 */
bool expired(Timer *timer);
/**
 * @brief Create a timer (milliseconds)
 *
 * Sets the timer to expire in a specified number of milliseconds.
 *
 * @param timer - pointer to the timer to be set to expire in milliseconds
 * @param timeout_ms - set the timer to expire in this number of milliseconds
 */
void countdown_ms(Timer *timer, unsigned int timeout_ms);
/**
 * @brief Create a timer (seconds)
 *
 * Sets the timer to expire in a specified number of seconds.
 *
 * @param timer - pointer to the timer to be set to expire in seconds
 * @param timeout - set the timer to expire in this number of seconds
 */
void countdown(Timer *timer, unsigned int timeout);
/**
 * @brief Check the time remaining on a give timer
 *
 * Checks the input timer and returns the number of milliseconds remaining on the timer.
 *
 * @param timer - pointer to the timer to be set to checked
 * @return int - milliseconds left on the countdown timer
 */
int left_ms(Timer *timer);
/**
 * @brief Initialize a timer
 *
 * Performs any initialization required to the timer passed in.
 *
 * @param timer - pointer to the timer to be initialized
 */
void InitTimer(Timer *timer);
#ifdef __cplusplus
}
#endif
#endif //QCLOUD_IOT_UTILS_TIMER_H_