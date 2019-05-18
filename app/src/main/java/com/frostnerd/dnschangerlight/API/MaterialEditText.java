package com.frostnerd.dnschangerlight.API;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.frostnerd.dnschangerlight.R;


/**
 * Copyright Daniel Wolf 2017
 * All rights reserved.
 *
 * This class contains small parts of work done by florent37 (https://github.com/florent37/MaterialTextField) and is heavily rewritten.
 * <p>
 * development@frostnerd.com
 */
public final class MaterialEditText extends RelativeLayout {
    protected TextView label;
    protected View card,indicator;
    protected ImageView icon;
    protected TextView indicatorText;
    protected EditText editText;
    protected ViewGroup editTextWrap;
    protected boolean expanded = false, firstDrawn = true,manualExpand = false;
    protected Settings settings = new Settings();
    protected int topMargin;

    public IndicatorState getIndicatorState(){
        return settings.indicatorState;
    }

    public MaterialEditText(Context context) {
        super(context);
    }

    public MaterialEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        handleAttributes(context,attrs);
    }

    public MaterialEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        handleAttributes(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MaterialEditText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        handleAttributes(context,attrs);
    }

    protected EditText findEditTextChild() {
        if (getChildCount() > 0 && getChildAt(0) instanceof EditText) {
            return (EditText) getChildAt(0);
        }
        return null;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        editText = findEditTextChild();
        if (editText == null)return;
        addView(LayoutInflater.from(getContext()).inflate(R.layout.material_edittext, this, false));

        editTextWrap = (ViewGroup) findViewById(R.id.met_edittext_wrap);
        indicator = findViewById(R.id.met_indicator);
        indicatorText = (TextView)findViewById(R.id.met_indicator_text);
        label = (TextView) findViewById(R.id.met_label);
        card = findViewById(R.id.met_card);

        removeView(editText);
        editTextWrap.addView(editText);
        ViewCompat.setPivotX(label, 0);
        ViewCompat.setPivotY(label, 0);
        if (editText.getHint() != null) {
            label.setText(editText.getHint());
            editText.setHint("");
        }

        final int expandedHeight = getContext().getResources().getDimensionPixelOffset(R.dimen.mte_card_expanded);
        final int reducedHeight = settings.cardCollapsedHeight;

        settings.reducedScale = (reducedHeight*1.0F/expandedHeight);
        ViewCompat.setScaleY(card, settings.reducedScale);
        ViewCompat.setPivotY(card, expandedHeight);

        icon = (ImageView) findViewById(R.id.met_icon);
        ViewCompat.setAlpha((View) icon, 0);
        ViewCompat.setScaleX(icon, 0.4f);
        ViewCompat.setScaleY(icon, 0.4f);

        ViewCompat.setAlpha(editText, 0f);
        editText.setBackgroundColor(Color.TRANSPARENT);
        editText.setEnabled(false);
        applyAttributes();
        topMargin = LayoutParams.class.cast(label.getLayoutParams()).topMargin;
        if(settings.revealType == RevealType.INSTANT && !expanded && firstDrawn)expand();
        if(settings.revealType == RevealType.HIDE_FOCUSLOSS && firstDrawn)editText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus && !manualExpand)collapse();
            }
        });
        firstDrawn = false;
        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                manualExpand = true;
                toggle();
            }
        });
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(settings.minCharLength != -1){
                    indicatorText.setText(s.length() + "/" + settings.minCharLength + "+");
                    if(s.length() >= settings.minCharLength)settings.indicatorState = IndicatorState.CORRECT;
                    else settings.indicatorState = IndicatorState.INCORRECT;
                }else if( settings.maxCharLength != -1){
                    indicatorText.setText(s.length() + "/" + settings.maxCharLength);
                    if(s.length() >= settings.maxCharLength)settings.indicatorState = IndicatorState.INCORRECT;
                    else settings.indicatorState = IndicatorState.CORRECT;
                }
                if(settings.minCharLength != -1 || settings.maxCharLength != -1)applyAttributes();
            }
        });
        if(settings.minCharLength != -1 || settings.maxCharLength != -1){
            indicatorText.setText("0/" + (settings.minCharLength != -1 ? settings.minCharLength + "+" : settings.maxCharLength));
            settings.indicatorState = IndicatorState.INCORRECT;
            applyAttributes();
        }
        manualExpand = false;
    }

    public void toggle(){
        if(expanded && settings.allowCollapse)collapse();
        else if (!expanded)expand();
    }

    public void expand(){
        if(expanded)return;
        editText.setEnabled(true);
        ViewCompat.animate(editText).alpha(1f).setDuration(settings.animationDuration).setStartDelay(settings.revealDelay);
        ViewCompat.animate(card).alpha(1f).scaleY(1f).setDuration(settings.animationDuration).setStartDelay(settings.revealDelay);
        ViewCompat.animate(label).scaleX(0.7f).scaleY(0.7f).translationY(-topMargin).setDuration(settings.animationDuration).setStartDelay(settings.revealDelay);
        ViewCompat.animate(icon).alpha(1f).scaleY(1f).scaleX(1f).setDuration(settings.animationDuration).setStartDelay(settings.revealDelay);
        label.setTextColor(settings.labelColorSecondary);
        editText.requestFocus();
        indicator.setVisibility(View.VISIBLE);
        indicatorText.setVisibility(View.VISIBLE);
        if(settings.openKeyboardOnFocus)((InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
        expanded = !expanded;
    }

    public void collapse(){
        if(!expanded)return;
        editText.setEnabled(false);
        final int heightInitial = getContext().getResources().getDimensionPixelOffset(R.dimen.mte_card_expanded);
        ViewCompat.animate(label).alpha(1).scaleX(1).scaleY(1).translationY(0).setDuration(settings.animationDuration);
        ViewCompat.animate(icon).alpha(0).scaleY(0.4f).scaleX(0.4f).setDuration(settings.animationDuration);
        ViewCompat.animate(editText).alpha(0f).setDuration(settings.animationDuration);
        ViewCompat.animate(card).scaleY(settings.reducedScale).setDuration(settings.animationDuration);
        label.setTextColor(settings.labelColorPrimary);
        InputMethodManager imm = ((InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE));
        if(editText.hasFocus() && imm.isAcceptingText())imm.hideSoftInputFromWindow(getWindowToken(),0);
        //if(settings.openKeyboardOnFocus)((InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
        editText.clearFocus();
        indicator.setVisibility(View.INVISIBLE);
        indicatorText.setVisibility(View.INVISIBLE);
        expanded = !expanded;
    }

    private void handleAttributes(Context context, AttributeSet attributeSet){
        TypedArray a = context.obtainStyledAttributes(attributeSet,R.styleable.MaterialEditText);
        settings.labelColorPrimary = a.getColor(R.styleable.MaterialEditText_labelColorPrimary,Color.WHITE);
        settings.labelColorSecondary = a.getColor(R.styleable.MaterialEditText_labelColorSecondary,Color.parseColor("#E0E0E0"));
        settings.minCharLength = a.getInt(R.styleable.MaterialEditText_minCharLength,-1);
        settings.maxCharLength = a.getInt(R.styleable.MaterialEditText_maxCharLength,-1);
        settings.iconID = a.getResourceId(R.styleable.MaterialEditText_image,-1);
        settings.indicatorState = IndicatorState.fromID(a.getInt(R.styleable.MaterialEditText_indicatorState,2));
        settings.animationDuration = a.getInt(R.styleable.MaterialEditText_animationDuration,400);
        settings.openKeyboardOnFocus = a.getBoolean(R.styleable.MaterialEditText_openKeyboardOnFocus,false);
        //settings.labelPosition = Position.fromPosID(a.getInt(R.styleable.MaterialEditText_labelPosition,1));
        settings.revealDelay = a.getInt(R.styleable.MaterialEditText_revealDelay,0);
        settings.revealType = RevealType.fromTypeID(a.getInt(R.styleable.MaterialEditText_revealType, RevealType.ON_CLICK.getTypeID()));
        settings.cardCollapsedHeight =  a.getDimensionPixelOffset(R.styleable.MaterialEditText_cardCollapsedHeight, context.getResources().getDimensionPixelOffset(R.dimen.mte_card_collapsed));
        settings.allowCollapse = a.getBoolean(R.styleable.MaterialEditText_allowCollapse,true);
        settings.labelText = a.getString(R.styleable.MaterialEditText_labelText);
        settings.cardColor = a.getColor(R.styleable.MaterialEditText_cardColor,Color.parseColor("#f6eef1"));
        settings.cardStrokeColor = a.getColor(R.styleable.MaterialEditText_cardStrokeColor,Color.parseColor("#E0E0E0"));
        settings.indicatorColorCorrect = a.getColor(R.styleable.MaterialEditText_indicatorColorCorrect,Color.parseColor("#4CAF50"));
        settings.indicatorColorIncorrect = a.getColor(R.styleable.MaterialEditText_indicatorColorIncorrect,Color.parseColor("#F44336"));
        settings.indicatorVisibilityWhenUnused = a.getInt(R.styleable.MaterialEditText_indicatorVisibilityWhenUnused, 0) == 0 ? View.INVISIBLE : View.GONE;
        if(settings.labelText == null)settings.labelText = "Label text";
        a.recycle();
    }

    public void applyAttributes(){
        label.setTextColor(expanded ? settings.labelColorSecondary : settings.labelColorPrimary);
        if(editText.getHint() == null)label.setText(settings.labelText);
        if(settings.iconID != -1)icon.setImageDrawable(getDrawable(getContext(),settings.iconID));
        else icon.setImageDrawable(null);
        if(settings.indicatorState == IndicatorState.UNDEFINED)indicator.setVisibility(View.INVISIBLE);
        else if(settings.indicatorState == IndicatorState.CORRECT){
            indicator.setVisibility(View.VISIBLE);
            indicatorText.setVisibility(View.VISIBLE);
            indicator.setBackgroundColor(settings.indicatorColorCorrect);
        }else if(settings.indicatorState == IndicatorState.INCORRECT){
            indicator.setVisibility(View.VISIBLE);
            indicatorText.setVisibility(View.VISIBLE);
            indicator.setBackgroundColor(settings.indicatorColorIncorrect);
        }
        indicatorText.setTextColor(settings.labelColorSecondary);
        if(settings.indicatorText != null && !settings.indicatorText.equals(""))indicatorText.setText(settings.indicatorText);
        if(settings.minCharLength == -1 && settings.maxCharLength == -1)indicatorText.setText("");
        indicatorText.setVisibility(indicatorText.getText().equals("") ? settings.indicatorVisibilityWhenUnused: View.VISIBLE);
        indicator.setVisibility(settings.indicatorState == IndicatorState.UNDEFINED ? settings.indicatorVisibilityWhenUnused : View.VISIBLE);
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(settings.cardColor);
        gd.setCornerRadius(4);
        gd.setStroke(1,settings.cardStrokeColor);
        gd.setShape(GradientDrawable.RECTANGLE);
        setBackground(card,gd);
    }

    public static Drawable getDrawable(Context c, int id){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return c.getResources().getDrawable(id,c.getTheme());
        }else return c.getResources().getDrawable(id);
    }

    @SuppressLint("NewApi")
    public static void setBackground(View v, Drawable background){
        int sdk = Build.VERSION.SDK_INT;
        if(sdk < Build.VERSION_CODES.JELLY_BEAN) {
            v.setBackgroundDrawable(background);
        } else if(sdk >= Build.VERSION_CODES.JELLY_BEAN){
            v.setBackground(background);
        }
    }

    protected TextView getLabel() {
        return label;
    }

    protected View getCard() {
        return card;
    }

    protected ImageView getIcon() {
        return icon;
    }

    protected EditText getEditText() {
        return editText;
    }

    protected ViewGroup getEditTextWrap() {
        return editTextWrap;
    }

    protected boolean isExpanded() {
        return expanded;
    }

    protected Settings getSettings() {
        return settings;
    }

    public View getIndicator() {
        return indicator;
    }

    public TextView getIndicatorText() {
        return indicatorText;
    }

    public void setIndicatorState(IndicatorState state){
        settings.indicatorState = state;
        applyAttributes();
    }

    public void setIndicatorText(String text){
        settings.indicatorText = text;
        applyAttributes();
    }

    public void clearIndicatorText(){
        settings.indicatorText = "";
        applyAttributes();
    }

    public void removeIcon(){
        settings.iconID = -1;
        applyAttributes();
    }

    public void setLabelColorPrimary(int color){
        settings.labelColorPrimary = color;
        applyAttributes();
    }

    public void setLabelColorSecondary(int color){
        settings.labelColorSecondary = color;
        applyAttributes();
    }

    public void setLabelColor(int primary,int secondary){
        settings.labelColorSecondary = secondary;
        settings.labelColorPrimary = primary;
        applyAttributes();
    }

    public void setMinCharLength(int min){
        if(min <= 0)throw new IllegalArgumentException("Min character length must be >= 1!");
        settings.minCharLength = min;
        indicatorText.setText(getEditText().getText().length() + "/" + settings.minCharLength + "+");
        if(getEditText().getText().length() < settings.minCharLength)settings.indicatorState = IndicatorState.INCORRECT;
        else settings.indicatorState = IndicatorState.CORRECT;
        applyAttributes();
    }

    public void setMaxCharLength(int max){
        if(max <= 0)throw new IllegalArgumentException("Max character length must be >= 1!");
        settings.maxCharLength = max;
        indicatorText.setText(getEditText().getText().length() + "/" + (settings.minCharLength != -1 ? settings.minCharLength + "+" : settings.maxCharLength));
        if(getEditText().getText().length() > settings.maxCharLength)settings.indicatorState = IndicatorState.INCORRECT;
        else settings.indicatorState = IndicatorState.CORRECT;
        applyAttributes();
    }

    public void clearMinCharLength(){
        settings.minCharLength = -1;
        if(settings.maxCharLength == -1 && settings.minCharLength == -1)settings.indicatorState = IndicatorState.UNDEFINED;
        applyAttributes();
    }

    public void clearMaxCharLength(){
        settings.maxCharLength = -1;
        if(settings.maxCharLength == -1 && settings.minCharLength == -1)settings.indicatorState = IndicatorState.UNDEFINED;
        applyAttributes();
    }

    public void setIcon(int resourceID){
        settings.iconID = resourceID;
        applyAttributes();
    }

    public void setAnimationDuration(int duration){
        settings.animationDuration = duration;
    }

    public void setOpenKeyboardOnFocus(boolean openKeyboardOnFocus){
        settings.openKeyboardOnFocus = openKeyboardOnFocus;
    }

    public void setRevealType(RevealType revealType){
        settings.revealType = revealType;
        if(settings.revealType == RevealType.HIDE_FOCUSLOSS)editText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus && !manualExpand)collapse();
            }
        });
        else editText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
            }
        });
    }

    public void setRevealDelay(int delay){
        settings.revealDelay = delay;
    }

    public void setCardCollapsedHeight(int height){
        settings.cardCollapsedHeight = height;
    }

    public void setLabelText(String text){
        settings.labelText = text;
        label.setText(text);
    }

    public void setCardColor(int color){
        settings.cardColor = color;
        applyAttributes();
    }

    public void setCardStrokeColor(int color){
        settings.cardStrokeColor = color;
        applyAttributes();
    }

    public void revealInstant(){
        int dur = settings.animationDuration;
        settings.animationDuration = 0;
        expand();
        settings.animationDuration = dur;
    }

    public final class Settings{
        protected int labelColorPrimary = Color.WHITE;
        protected int labelColorSecondary = Color.parseColor("#E0E0E0"),cardColor = Color.parseColor("#f6eef1"),cardStrokeColor = Color.parseColor("#E0E0E0");
        protected int minCharLength = -1;
        protected int maxCharLength = -1;
        protected int iconID = -1, indicatorColorCorrect = Color.parseColor("#4CAF50"),indicatorColorIncorrect = Color.parseColor("#F44336");
        protected IndicatorState indicatorState = IndicatorState.UNDEFINED;
        protected int animationDuration = 400;
        protected boolean openKeyboardOnFocus = false;
        protected Position labelPosition = Position.TOP;
        protected RevealType revealType = RevealType.ON_CLICK;
        protected int revealDelay = 0;
        protected float reducedScale = 0.2f;
        protected int cardCollapsedHeight;
        protected boolean allowCollapse = true;
        protected String indicatorText = "";
        protected String labelText = "Label text";
        protected int indicatorVisibilityWhenUnused = View.INVISIBLE;


        public int getLabelColorPrimary() {
            return labelColorPrimary;
        }

        public int getLabelColorSecondary() {
            return labelColorSecondary;
        }

        public int getMinCharLength() {
            return minCharLength;
        }

        public int getMaxCharLength() {
            return maxCharLength;
        }

        public int getIconID() {
            return iconID;
        }

        public IndicatorState getIndicatorState() {
            return indicatorState;
        }

        public int getAnimationDuration() {
            return animationDuration;
        }

        public boolean isOpenKeyboardOnFocus() {
            return openKeyboardOnFocus;
        }

        public Position getLabelPosition() {
            return labelPosition;
        }

        public RevealType getRevealType() {
            return revealType;
        }

        public int getRevealDelay() {
            return revealDelay;
        }

        public float getReducedScale() {
            return reducedScale;
        }

        public int getCardCollapsedHeight() {
            return cardCollapsedHeight;
        }

        public boolean isAllowCollapse() {
            return allowCollapse;
        }

        public String getIndicatorText() {
            return indicatorText;
        }

        public String getLabelText() {
            return labelText;
        }

        public int getCardColor() {
            return cardColor;
        }

        public int getCardStrokeColor() {
            return cardStrokeColor;
        }
    }

    public enum RevealType{
        ON_CLICK(0),INSTANT(1),HIDE_FOCUSLOSS(2);

        private final int typeID;

        RevealType(int i){
            typeID = i;
        }

        public int getTypeID(){
            return typeID;
        }

        public static RevealType fromTypeID(int typeID){
            for(RevealType r:values())if(r.getTypeID() == typeID)return r;
            return null;
        }
    }

    public enum Position{
        TOP(1),BOTTOM(0);

        private final int posID;

        Position(int i){
            this.posID = i;
        }

        public int getPosID(){
            return posID;
        }

        public static Position fromPosID(int posID){
            for(Position p: values())if(p.getPosID() == posID)return p;
            return null;
        }
    }

    public enum IndicatorState{
        INCORRECT(0),CORRECT(1),UNDEFINED(2);

        private final int id;

        IndicatorState(int i){
            this.id = i;
        }

        public int getID(){
            return id;
        }

        public static IndicatorState fromID(int id){
            for(IndicatorState i: values())if(i.getID() == id)return i;
            return null;
        }
    }
}