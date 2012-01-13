/**
 * Copyright (C) 2011-2012 NZDIS.org. All Rights Reserved. See AUTHORS and LICENCE.
 */
package org.nzdis;

import android.app.Activity;
import android.os.Bundle;

/**
 * Provides user with basic instructions of how to use the app.
 * 
 * @author Hamish Medlin
 *
 * @version $Revision$ <br>
 * Created: Dec 2011
 */
public class InstructionsActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructions);
    }
    
}
