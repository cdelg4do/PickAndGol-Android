package io.keepcoding.pickandgol.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


/**
 * This class defines an Spinner adapter for pairs < key, value >,
 * where the key is an Integer and the value is a String.
 */
public class IntegerStringSpinnerAdapter extends BaseAdapter {

    private int layoutId;

    private final Integer[] keys;
    private final String[] values;

    private final int size;
    private LayoutInflater inflater;

    /**
     * Sets the data for the Spinner.
     *
     * @param context        context for the spinner opertations.
     * @param keys           array with the keys (without value 0).
     * @param values         array with the values to show.
     * @param defaultText    text of the default selection (assigned to value 0).
     */
    public IntegerStringSpinnerAdapter(Context context, Integer[] keys, String[] values, String defaultText)
    {
        this.layoutId   = android.R.layout.simple_spinner_item;

        this.size = keys.length;

        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        this.keys = new Integer[ size + 1 ];
        this.values = new String[ size + 1 ];

        this.keys[0] = 0;
        this.values[0] = defaultText;

        for (int i=0; i<size; i++) {
            this.keys[i+1] = keys[i];
            this.values[i+1] = values[i];
        }
    }


    @Override
    public int getCount() {
        return size + 1;
    }


    public int getKeyPosition(Integer key) {

        for (int i=0; i< keys.length; i++)
            if ( keys[i].equals(key) )
                return i;

        return -1;
    }


    @Override
    public String getItem(int pos) {
        return values[pos];
    }


    @Override
    public long getItemId(int pos) {
        return keys[pos];
    }


    public View getView(int pos, View view, ViewGroup parent) {

        if(view == null)
            view = inflater.inflate(layoutId, parent, false);

        TextView text  = (TextView) view.findViewById(android.R.id.text1);
        text.setText(values[pos]);

        return view;
    }

}
