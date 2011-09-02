/*
 * STAException.java
 *
 * Created on 02 decembrie 2007, 12:22
 *
 * Created on 17 martie 2007, 11:14
 *
 * DSHub ADC HubSoft
 * Copyright (C) 2007,2008  Eugen Hristev
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package ru.sincore.Exceptions;

/**
 * @author Pietricica
 */
public class STAException extends Exception
{
    public int x;


    public STAException()
    {
        super();
    }


    ;


    public STAException(String bla, int x)
    {
        super(bla);
        this.x = x;

    }
};
