package controllers;

import ninja.FilterWith;

import com.google.inject.Singleton;

import filters.AuthenticationFilter;
import filters.AppFilter;

/**
 * 
 * @author svenkubiak
 *
 */
@Singleton
@FilterWith({AppFilter.class, AuthenticationFilter.class})
public class RootController {
}