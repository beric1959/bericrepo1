package ru.diasoft.platform.services.lk_sbamws.command.product;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация разрешает копировать объект
 * 
 * @author Dilmukhamedov
 * 
 */
@Target( { ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface CopyAnnotation {
}
