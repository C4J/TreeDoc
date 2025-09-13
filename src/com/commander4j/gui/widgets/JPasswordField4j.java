package com.commander4j.gui.widgets;

import java.awt.Color;
import java.awt.event.FocusAdapter;

/**
 * @author David Garratt
 * 
 * Project Name : Commander4j
 * 
 * Filename     : JPasswordField4j.java
 * 
 * Package Name : com.commander4j.gui
 * 
 * License      : GNU General Public License
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * http://www.commander4j.com/website/license.html.
 * 
 */

import java.awt.event.FocusEvent;

import javax.swing.JPasswordField;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;

import com.commander4j.filters.JFixedSizeFilter;
import com.commander4j.settings.Common;

public class JPasswordField4j extends JPasswordField
{

    private static final long serialVersionUID = 1L;
    private final Color overflowForeground = Color.RED;
    private JFixedSizeFilter tsf;
    private int characterLimit = -1; // -1 means "no limit"
    private static final Border EMPTY_BORDER = new LineBorder(Color.GRAY);
    private boolean hasFocus = false;
    
	private void init()
	{
        setDisabledTextColor(Common.color_textfield_foreground_disabled);
        setBorder(EMPTY_BORDER);
		setFont(Common.font_input);
	}

    public JPasswordField4j() {
        super();
        init();
        initFocusBehavior();
        updateColors();
    }
    
	public JPasswordField4j(String text) {
		super(text);
		init();
        initFocusBehavior();
        updateColors();
	}

    public JPasswordField4j(int columns) {
        super(columns);
        init();
        this.characterLimit = columns;
        initFocusBehavior();
        initCharacterLimitBehavior();
        updateColors();
    }

    private void initFocusBehavior() {
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
            	hasFocus = true;
            	if (isEditable())
            	{
                    setBackground(Common.color_textfield_background_focus_color);
            	}

            }

            @Override
            public void focusLost(FocusEvent e) {
            	hasFocus = false;
            	if (isEditable())
            	{
                setBackground(Common.color_textfield_background_nofocus_color);
            	}
            }
        });
    }

    private void initCharacterLimitBehavior() {
        if (characterLimit > 0) {
            getDocument().addDocumentListener(new DocumentListener() {
                public void insertUpdate(DocumentEvent e) {
                    updateTextColor();
                }

                public void removeUpdate(DocumentEvent e) {
                    updateTextColor();
                }

                public void changedUpdate(DocumentEvent e) {
                    updateTextColor();
                }

                private void updateTextColor() {
                    if (getPassword().toString().length() >= characterLimit) {
                        setForeground(overflowForeground);
                    } else {
                        updateColors();
                    }
                }
            });
    
    		tsf = new JFixedSizeFilter(characterLimit);
    	    ((AbstractDocument) getDocument()).setDocumentFilter(tsf);
        }
    }

    public void setCharacterLimit(int limit) {
        if (characterLimit < 0 && limit > 0) {
            this.characterLimit = limit;
            initCharacterLimitBehavior();
        } else {
            this.characterLimit = limit;
        }
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        updateColors();
    }

    @Override
    public void setEditable(boolean editable) {
        super.setEditable(editable);
        updateColors();
    }
    
    @Override
    public void updateUI() {
        super.updateUI();
        setDisabledTextColor(Common.color_textfield_foreground_disabled);
        updateColors();
    }

    private void updateColors() {
        if (!isEnabled()) {
            setBackground(Common.color_textfield_background_disabled);
            setForeground(Common.color_textfield_foreground_disabled);
        } else if (!isEditable()) {
            setBackground(Common.color_textfield_background_disabled);
            setForeground(Common.color_textfield_foreground_disabled);
        } else if (hasFocus){
            setBackground(Common.color_textfield_background_focus_color);
            setForeground(Common.color_textfield_foreground_focus_color);
        } else
        {
            setBackground(Common.color_textfield_background_nofocus_color);
            setForeground(Common.color_textfield_forground_nofocus_color);	
        }
        
    }
    
}
