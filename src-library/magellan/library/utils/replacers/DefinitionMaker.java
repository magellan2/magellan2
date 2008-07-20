/*
 *  Copyright (C) 2000-2004 Roger Butenuth, Andreas Gampe,
 *                          Stefan Goetz, Sebastian Pappert,
 *                          Klaas Prause, Enno Rehling,
 *                          Sebastian Tusk, Ulrich Kuester,
 *                          Ilja Pavkovic
 *
 * This file is part of the Eressea Java Code Base, see the
 * file LICENSING for the licensing information applying to
 * this file.
 *
 */

/*
 * DefinitionMaker.java
 *
 * Created on 19. Mai 2002, 12:04
 */
package magellan.library.utils.replacers;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

/**
 * DOCUMENT ME!
 *
 * @author Andreas
 * @version 1.0
 */
public class DefinitionMaker {
	/** DOCUMENT-ME */
	public static final String ESCAPE = "\\";
	protected String unknown = "";

	/**
	 * Creates new DefinitionMaker
	 */
	public DefinitionMaker() {
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void setUnknown(String unknown) {
		this.unknown = unknown;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public String getUnknown() {
		return unknown;
	}

	protected static String scanEscapes(String s, ReplacerFactory factory) {
		// look for escape string
		if(s.startsWith(DefinitionMaker.ESCAPE)) {
			String s2 = s.substring(1);

			if((s2.length() > 0) && factory.isReplacer(s2)) {
				s = s2;
			}
		}

		return s;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 * 
	 *
	 * 
	 */
	public ReplacerSystem createDefinition(String defStr, String cmdChars, ReplacerFactory factory) {
		return DefinitionMaker.createDefinition(defStr, cmdChars, factory, unknown);
	}

	/**
	 *
	 */
	public static ReplacerSystem createDefinition(String defStr, String cmdChars, ReplacerFactory factory, String unknown) {
		ReplacerSystem sys = new ReplacerSystem();

		if((defStr == null) || (defStr.length() == 0)) {
			sys.setBase(new ListReplacer(null, unknown));

			return sys;
		}

		StringTokenizer st = new StringTokenizer(defStr, cmdChars);

		if(st.countTokens() == 0) {
			sys.setBase(new ListReplacer(null, unknown));

			return sys;
		}

		if(st.countTokens() == 1) {
			String s = st.nextToken();

			if(factory.isReplacer(s)) {
				Replacer rep = factory.createReplacer(s);

				if((rep instanceof ParameterReplacer) || (rep instanceof BranchReplacer)) {
					sys.setBase(new ListReplacer(null, unknown));

					return sys;
				}

				if(rep instanceof EnvironmentDependent) {
					((EnvironmentDependent) rep).setEnvironment(sys.getEnvironment());
				}

				sys.setBase(rep);

				return sys;
			}

			sys.setBase(new SimpleStringReplacer(DefinitionMaker.scanEscapes(s, factory)));

			return sys;
		}

		sys.setBase(DefinitionMaker.createListReplacer(st, factory, unknown, sys));

		return sys;
	}

	protected static Replacer createBranchReplacer(BranchReplacer branch, StringTokenizer st,
												   ReplacerFactory factory, String unknown,
												   ReplacerSystem env) {
		int branches = branch.getBranchCount();

		if(branches == -1) {
			return DefinitionMaker.createSwitchBranchReplacer(branch, st, factory, unknown, env);
		}

		String repEnd = branch.getBranchSign(branches);

		// collect all structure data, but return false
		for(int i = 0; i < branches; i++) {
			String branchEnd = branch.getBranchSign(i + 1);
			LinkedList<Object> subList = new LinkedList<Object>();
			boolean endReached = false;
			boolean repEndReached = false;

			while(!endReached && !repEndReached && st.hasMoreTokens()) {
				String token = st.nextToken();
				endReached = token.equals(branchEnd);
				repEndReached = token.equals(repEnd);

				if(!endReached && !repEndReached) {
					if(factory.isReplacer(token)) {
						subList.add(DefinitionMaker.createReplacer(token, st, factory, unknown, env));
					} else {
						subList.add(DefinitionMaker.scanEscapes(token, factory));
					}
				}
			}

			if(subList.size() > 0) {
				if(subList.size() == 1) {
					branch.setBranch(i, subList.get(0));
				} else {
					branch.setBranch(i, new ListReplacer(subList, unknown));
				}
			} else {
				branch.setBranch(i, new ListReplacer(null, unknown));
			}

			if(repEndReached) {
				break;
			}
		}

		return branch;
	}

	protected static Replacer createSwitchBranchReplacer(BranchReplacer branch, StringTokenizer st,
														 ReplacerFactory factory, String unknown,
														 ReplacerSystem env) {
		String branchEnd = branch.getBranchSign(0);
		String switchEnd = branch.getBranchSign(1);
		LinkedList<Object> subList = new LinkedList<Object>();
		boolean endReached = false;
		int branchCount = 0;
		boolean anything = false;

		while(!endReached && st.hasMoreTokens()) {
			boolean isBranchEnd = false;

			while(!isBranchEnd && st.hasMoreTokens()) {
				String token = st.nextToken();
				isBranchEnd = token.equals(branchEnd);
				endReached = token.equals(switchEnd);

				if(!isBranchEnd && !endReached) {
					if(factory.isReplacer(token)) {
						subList.add(DefinitionMaker.createReplacer(token, st, factory, unknown, env));
					} else {
						subList.add(DefinitionMaker.scanEscapes(token, factory));
					}
				}
			}

			if(subList.size() == 0) {
				branch.setBranch(branchCount, null);
			} else if(subList.size() == 1) {
				anything = true;
				branch.setBranch(branchCount, subList.get(0));
			} else {
				anything = true;
				branch.setBranch(branchCount, new ListReplacer(subList, unknown));
			}
		}

		if(anything) {
			return branch;
		}

		return new ListReplacer(null, unknown);
	}

	protected static Replacer createListReplacer(StringTokenizer st, ReplacerFactory factory,
												 String unknown, ReplacerSystem env) {
		List<Object> list = new LinkedList<Object>();

		while(st.hasMoreTokens()) {
			String token = st.nextToken();

			if(factory.isReplacer(token)) {
				list.add(DefinitionMaker.createReplacer(token, st, factory, unknown, env));
			} else {
				list.add(DefinitionMaker.scanEscapes(token, factory));
			}
		}

		if(list.size() == 0) {
			return new ListReplacer(null, unknown);
		} else if(list.size() == 1) {
			Object obj = list.get(0);

			if(obj instanceof Replacer) {
				return (Replacer) obj;
			}

			return new SimpleStringReplacer(obj.toString());
		}

		return new ListReplacer(list, unknown);
	}

	protected static Replacer createReplacer(String token, StringTokenizer st,
											 ReplacerFactory factory, String unknown,
											 ReplacerSystem env) {
		Replacer rep = factory.createReplacer(token);

		if(rep instanceof EnvironmentDependent) {
			((EnvironmentDependent) rep).setEnvironment(env.getEnvironment());
		}

		if(rep instanceof ParameterReplacer) {
			rep = DefinitionMaker.createParameterReplacer((ParameterReplacer) rep, st, factory, unknown, env);
		}

		if(rep instanceof BranchReplacer) {
			rep = DefinitionMaker.createBranchReplacer((BranchReplacer) rep, st, factory, unknown, env);
		}

		return rep;
	}

	protected static Replacer createParameterReplacer(ParameterReplacer param, StringTokenizer st,
													  ReplacerFactory factory, String unknown,
													  ReplacerSystem env) {
		int params = param.getParameterCount();

		if(params > 0) {
			for(int i = 0; i < params; i++) {
				if(st.hasMoreTokens()) {
					try {
						List<Object> helpList = null;
						Object o = null;
						boolean nonSwitch = false;

						do {
							String token = st.nextToken();
							o = null;

							if(factory.isReplacer(token)) {
								o = DefinitionMaker.createReplacer(token, st, factory, unknown, env);
							} else {
								o = DefinitionMaker.scanEscapes(token, factory);
							}

							if(!(o instanceof SwitchOnly)) {
								nonSwitch = true;

								if(helpList != null) {
									helpList.add(o);
								}
							} else {
								if(helpList == null) {
									helpList = new ArrayList<Object>(2);
								}

								helpList.add(o);
							}
						} while(!nonSwitch);

						if(helpList != null) {
							param.setParameter(i, new ListReplacer(helpList, unknown));
						} else {
							param.setParameter(i, o);
						}
					} catch(NoSuchElementException exc) {
						return new ListReplacer(null, unknown);
					}
				} else {
					return new ListReplacer(null, unknown);
				}
			}
		}

		return param;
	}
}
