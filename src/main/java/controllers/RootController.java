package controllers;

import ninja.FilterWith;

import com.google.inject.Singleton;

import filters.LanguageFilter;
import filters.AuthenticationFilter;
import filters.SetupFilter;

/**
 * 
 * @author svenkubiak
 *
 */
@Singleton
@FilterWith({LanguageFilter.class, SetupFilter.class, AuthenticationFilter.class})
public class RootController {}