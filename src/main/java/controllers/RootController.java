package controllers;

import ninja.FilterWith;

import com.google.inject.Singleton;

import filters.AuthenticationFilter;
import filters.SetupFilter;

/**
 * 
 * @author svenkubiak
 *
 */
@Singleton
@FilterWith({SetupFilter.class, AuthenticationFilter.class})
public class RootController {}